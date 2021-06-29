package org.sagebionetworks.research.mtb.alpha_app.ui.today

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.sagebionetworks.assessmentmodel.presentation.AssessmentActivity
import org.sagebionetworks.bridge.kmm.shared.cache.ResourceResult
import org.sagebionetworks.bridge.kmm.shared.models.AdherenceRecord
import org.sagebionetworks.bridge.kmm.shared.models.PerformanceOrder
import org.sagebionetworks.bridge.kmm.shared.repo.ScheduledAssessmentReference
import org.sagebionetworks.bridge.kmm.shared.repo.ScheduledSessionWindow
import org.sagebionetworks.research.mtb.alpha_app.MtbAssessmentActivity
import org.sagebionetworks.research.mtb.alpha_app.R
import org.sagebionetworks.research.mtb.alpha_app.databinding.FragmentTodayListBinding

class TodayFragment : Fragment() {

    companion object {


        /**
         * Mapping from assessment ID defined in Bridge to value defined in local JSON files.
         * Ideally we should update the shared assessment identifiers to match what is in JSON.
         */
        val assessmentIdentifierMap = mapOf(
            "vocabulary" to "Vocabulary Form 1",
            "spelling" to "MTB Spelling Form 1",
            "psm" to "Picture Sequence MemoryV1",
            "number-match" to "Number Match",
            "flanker" to "Flanker Inhibitory Control",
            "dccs" to "Dimensional Change Card Sort",
            "memory-for-sequences" to "MFS pilot 2",
            "fnamea" to "FNAME Learning Form 1",
            "fnameb" to "FNAME Test Form 1"
        )

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
            "fnameb" to R.drawable.ic_as_faces_names_b
        )

    }

    private val viewModel: TodayViewModel by viewModel()
    lateinit var binding: FragmentTodayListBinding
    lateinit var listAdapter: TodayRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.sessionLiveData.observe(this, Observer {
            when(it) {
                is ResourceResult.Success -> {
                    sessionsLoaded(it.data)
                }
                is ResourceResult.InProgress -> {

                }
                is ResourceResult.Failed -> {

                }
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTodayListBinding.inflate(inflater)
        listAdapter = TodayRecyclerViewAdapter{assessment, session -> launchAssessment(assessment, session)}
        val headerAdapter = HeaderAdapter()

        // Set the adapter
        with(binding.list) {
            layoutManager =  LinearLayoutManager(context)
            adapter = ConcatAdapter(headerAdapter, listAdapter)
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadTodaysSessions()
    }

    private fun sessionsLoaded(sessions: List<ScheduledSessionWindow>) {
        //TODO: Create list of items to submit to list adapter
        val dataList = mutableListOf<DataItem>()
        var assessmentAdded = false
        sessions.forEach{
            assessmentAdded = assessmentAdded || addSession(it, dataList)
        }
        listAdapter.submitList(dataList)

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

    private fun addSession(session: ScheduledSessionWindow, dataList: MutableList<DataItem>) : Boolean {
        if (session.isCompleted || session.assessments.isEmpty()) return false //Don't add session if it is completed

        //Add HeaderItem
        dataList.add(SessionHeaderItem(session))
        var assessmentAdded = false
        for (assessmentRef in session.assessments) {

            val locked = assessmentAdded && session.sessionInfo.performanceOrder == PerformanceOrder.SEQUENTIAL
            if (!assessmentRef.isCompleted) {
                // Add AssessmentItem
                dataList.add(AssessmentItem(assessmentRef, locked, session))
                assessmentAdded = true
            }
        }
        return assessmentAdded
    }

    private fun launchAssessment(assessmentRef: ScheduledAssessmentReference, session: ScheduledSessionWindow) {
        val adherenceRecord = AdherenceRecord(
            instanceGuid = assessmentRef.instanceGuid,
            startedOn = Clock.System.now(),
            eventTimestamp = session.eventTimeStamp.toString())
        val assessmentId = assessmentIdentifierMap.get(assessmentRef.assessmentInfo.identifier) ?: assessmentRef.assessmentInfo.identifier
        val intent = Intent(requireActivity(), MtbAssessmentActivity::class.java)
        intent.putExtra(AssessmentActivity.ARG_ASSESSMENT_ID_KEY, assessmentId)
        intent.putExtra(MtbAssessmentActivity.ARG_ADHERENCE_RECORD_KEY, Json.encodeToString(adherenceRecord))
        intent.putExtra(
            MtbAssessmentActivity.ARG_SESSION_EXPIRATION_KEY, session.endDateTime.toInstant(
            TimeZone.currentSystemDefault()).toEpochMilliseconds())
        //Fix for June so that MTB assessments are full screen
        intent.putExtra(AssessmentActivity.ARG_THEME, edu.northwestern.mobiletoolbox.common.R.style.Theme_AppCompat_Light_NoActionBar_FullSizeScreen)
        startActivity(intent)
    }

}