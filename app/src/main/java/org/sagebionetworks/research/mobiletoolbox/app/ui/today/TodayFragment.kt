package org.sagebionetworks.research.mobiletoolbox.app.ui.today

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import co.touchlab.kermit.Logger
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.sagebionetworks.assessmentmodel.presentation.AssessmentActivity
import org.sagebionetworks.assessmentmodel.resourcemanagement.StandardResourceAssetType
import org.sagebionetworks.assessmentmodel.serialization.AssessmentInfoObject
import org.sagebionetworks.bridge.kmm.shared.cache.ResourceResult
import org.sagebionetworks.bridge.kmm.shared.models.AdherenceRecord
import org.sagebionetworks.bridge.kmm.shared.models.AssessmentInfo
import org.sagebionetworks.bridge.kmm.shared.models.PerformanceOrder
import org.sagebionetworks.bridge.kmm.shared.repo.AdherenceRecordRepo
import org.sagebionetworks.bridge.kmm.shared.repo.AuthenticationRepository
import org.sagebionetworks.bridge.kmm.shared.repo.ScheduledAssessmentReference
import org.sagebionetworks.bridge.kmm.shared.repo.ScheduledSessionTimelineSlice
import org.sagebionetworks.bridge.kmm.shared.repo.ScheduledSessionWindow
import org.sagebionetworks.research.mobiletoolbox.app.MtbAssessmentActivity
import org.sagebionetworks.research.mobiletoolbox.app.MtbBaseFragment
import org.sagebionetworks.research.mobiletoolbox.app.R
import org.sagebionetworks.research.mobiletoolbox.app.databinding.FragmentTodayListBinding
import org.sagebionetworks.research.mobiletoolbox.app.recorder.RecorderConfigViewModel
import org.sagebionetworks.research.mobiletoolbox.app.recorder.model.recorderConfigJsonCoder

class TodayFragment : MtbBaseFragment() {

    companion object {

        /**
         * Mapping from the assessment ID defined in Bridge to the Drawable to use.
         */
        val assessmentIconMap = mapOf(
            "vocabulary" to R.drawable.ic_as_word_meaning,
            "spelling" to R.drawable.ic_as_spelling,
            "psm" to R.drawable.ic_as_arranging_pictures,
            "number-match" to R.drawable.ic_as_number_symbol_match,
            "flanker" to R.drawable.ic_as_arrow_matching,
            "dccs" to R.drawable.ic_as_shape_color_sorting,
            "memory-for-sequences" to R.drawable.ic_as_sequences,
            "fnamea" to R.drawable.ic_as_faces_names_a,
            "fnameb" to R.drawable.ic_as_faces_names_b,
            "JOVIV1" to R.drawable.ic_as_eggs,
            "ChangeLocalizationV1" to R.drawable.ic_as_color_change,
            "VerbalReasoningV1" to R.drawable.ic_as_word_problems,
            "LetterNumberSeriesV1" to R.drawable.ic_as_letters_and_numbers,
            "3DRotationV1" to R.drawable.ic_as_block_rotation,
            "ProgressiveMatricesV1" to R.drawable.ic_as_puzzle_completion,
            "FaceEmotionV1" to R.drawable.ic_as_faces_and_feelings,
            "GradualOnsetV1" to R.drawable.ic_as_cities_and_mountains,
            "ProbabilisticRewardForm1V1" to R.drawable.ic_as_number_guessing,
            "ProbabilisticRewardForm2V1" to R.drawable.ic_as_number_guessing,
        )

        fun getAssessmentIcon(context: Context, assessmentInfo: AssessmentInfo) : Int {
            // First try to find icon resource identifier based on assessment identifier
            assessmentIconMap.get(assessmentInfo.identifier)?.let { resourceId ->
                return resourceId
            }
            // Next try the imageResource from AssessmentInfo
            assessmentInfo.imageResource?.let { imageResource ->
                val resourceName = imageResource.module?.let { module ->
                    module + "_" + imageResource.name
                } ?: imageResource.name
                val resourceId = context.resources.getIdentifier(resourceName, StandardResourceAssetType.DRAWABLE, context.packageName)
                if (resourceId != 0) {
                    return resourceId
                }
            }
            // Default to the default survey icon
            return R.drawable.sage_survey_default
        }
    }

    private val viewModel: TodayViewModel by viewModel()
    private val recorderConfigViewModel: RecorderConfigViewModel by viewModel()
    lateinit var binding: FragmentTodayListBinding
    private lateinit var listAdapter: TodayRecyclerViewAdapter
    private lateinit var headerAdapter: HeaderAdapter

    val adherenceRecordRepo: AdherenceRecordRepo by inject()
    val authRepo: AuthenticationRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        recorderConfigViewModel.recorderScheduledAssessmentConfig.observe(this, {
            Logger.d("Received RecorderScheduleAssessmentConfig: $it")
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTodayListBinding.inflate(inflater)
        listAdapter = TodayRecyclerViewAdapter { assessment, session ->
            launchAssessment(
                assessment,
                session
            )
        }
        headerAdapter = HeaderAdapter()
        headerAdapter.loading = true

        // Set the adapter
        with(binding.list) {
            layoutManager = LinearLayoutManager(context)
            adapter = ConcatAdapter(headerAdapter, listAdapter)
        }

        viewModel.sessionLiveData.observe(viewLifecycleOwner, Observer {
            when (it.second) {
                is ResourceResult.Success -> {
                    sessionsLoaded((it.second as ResourceResult.Success<ScheduledSessionTimelineSlice>).data.scheduledSessionWindows)
                }
                is ResourceResult.InProgress -> {
                    //Do nothing
                }
                is ResourceResult.Failed -> {
                    //Do nothing
                }
            }
        })
        viewModel.resultsUploading.observe(viewLifecycleOwner) {
            headerAdapter.resultsUploading = it.isNotEmpty()
            headerAdapter.hasInternet = true
            if (headerAdapter.resultsUploading) {
                val connectivityManager = getSystemService(requireContext(), ConnectivityManager::class.java)
                val caps = connectivityManager?.getNetworkCapabilities(connectivityManager.activeNetwork)
                headerAdapter.hasInternet = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false
            }
            headerAdapter.notifyDataSetChanged()
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        recorderConfigViewModel.loadRecorderConfigs()
        viewModel.loadTodaysSessions()
    }

    private fun sessionsLoaded(sessions: List<ScheduledSessionWindow>) {
        val dataList = mutableListOf<DataItem>()
        var availableAssessmentAdded = false
        sessions.forEach {
            availableAssessmentAdded = addSession(it, dataList) || availableAssessmentAdded
        }
        listAdapter.submitList(dataList)

        //Update header whether any assessments currently available
        headerAdapter.loading = false
        headerAdapter.todayComplete = !availableAssessmentAdded
        headerAdapter.notifyDataSetChanged()

        // Upload status for June release
//        if (sessions.isNotEmpty() && !assessmentAdded) {
//            binding.scrollView.visibility = View.GONE
//            binding.container.findViewById<View>(R.id.upload_status_view)?.let {
//                binding.container.removeView(it)
//            }
//            //User has completed all activities
//            if (uploadRequester.pendingUploads) {
//                //Show uploading view
//                binding.container.addView(AssessmentUploadingViewBinding.inflate(layoutInflater).root)
//            } else {
//                //Show uploads complete view
//                binding.container.addView(AssessmentUploadsCompleteViewBinding.inflate(layoutInflater).root)
//            }
//        }
    }

    private fun addSession(
        session: ScheduledSessionWindow,
        dataList: MutableList<DataItem>
    ): Boolean {
        if ((!session.persistent && session.isCompleted) || session.assessments.isEmpty()) return false //Don't add session if it is completed
        //TODO: Mark first future header so we can add "Up next" title
        //Add HeaderItem
        dataList.add(SessionHeaderItem(session))
        var availableAssessmentAdded = false
        for (assessmentRef in session.assessments) {

            val locked = session.isInFuture() ||
                    (availableAssessmentAdded && session.sessionInfo.performanceOrder == PerformanceOrder.SEQUENTIAL)
            val hide = !session.persistent && (assessmentRef.isCompleted || assessmentRef.isDeclined)
            if (!hide) {
                // Add AssessmentItem
                dataList.add(AssessmentItem(assessmentRef, locked, session))
                if (!locked) availableAssessmentAdded =
                    true //Track if an active assessment was added
            }
        }
        return availableAssessmentAdded
    }

    private fun launchAssessment(
        assessmentRef: ScheduledAssessmentReference,
        session: ScheduledSessionWindow
    ) {
        val adherenceRecord = AdherenceRecord(
            instanceGuid = assessmentRef.instanceGuid,
            startedOn = Clock.System.now(),
            eventTimestamp = session.eventTimestamp.toString(),
        )
        val assessmentId = viewModel
            .assessmentIdentifierMapLiveData.value!![assessmentRef.assessmentInfo.identifier]
            ?: assessmentRef.assessmentInfo.identifier
        val intent = Intent(requireActivity(), MtbAssessmentActivity::class.java)
        intent.putExtra(AssessmentActivity.ARG_ASSESSMENT_ID_KEY, assessmentId)

        val assessmentInfoObject = AssessmentInfoObject(
            identifier = assessmentId,
            guid = assessmentRef.assessmentInfo.guid
        )

        intent.putExtra(AssessmentActivity.ARG_ASSESSMENT_INFO_KEY, Json.encodeToString(assessmentInfoObject))

        intent.putExtra(
            MtbAssessmentActivity.ARG_ADHERENCE_RECORD_KEY,
            Json.encodeToString(adherenceRecord)
        )
        intent.putExtra(
            MtbAssessmentActivity.ARG_SESSION_EXPIRATION_KEY, session.endDateTime.toInstant(
                TimeZone.currentSystemDefault()
            ).toEpochMilliseconds()
        )
        intent.putExtra(
            MtbAssessmentActivity.ARG_RECORDER_CONFIG_KEY,
            recorderConfigJsonCoder.encodeToString(recorderConfigViewModel.recorderScheduledAssessmentConfig.value)
        )
        //Fix for June so that MTB assessments are full screen
        intent.putExtra(
            AssessmentActivity.ARG_THEME,
            edu.northwestern.mobiletoolbox.common.R.style.Theme_AppCompat_Light_NoActionBar_FullSizeScreen
        )
        startActivity(intent)
    }

}