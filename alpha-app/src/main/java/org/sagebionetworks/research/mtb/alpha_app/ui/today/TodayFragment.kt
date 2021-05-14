package org.sagebionetworks.research.mtb.alpha_app.ui.today

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Space
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import kotlinx.datetime.toJavaLocalDateTime
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.sagebionetworks.assessmentmodel.presentation.AssessmentActivity
import org.sagebionetworks.bridge.kmm.shared.cache.ResourceResult
import org.sagebionetworks.bridge.kmm.shared.repo.ScheduledAssessmentReference
import org.sagebionetworks.bridge.kmm.shared.repo.ScheduledSessionWindow
import org.sagebionetworks.research.mtb.alpha_app.MtbAssessmentActivity
import org.sagebionetworks.research.mtb.alpha_app.R
import org.sagebionetworks.research.mtb.alpha_app.databinding.DueDateHeaderBinding
import org.sagebionetworks.research.mtb.alpha_app.databinding.FragmentTodayBinding
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class TodayFragment : Fragment() {

    companion object {


        /**
         * Mapping from assessment ID defined in Bridge to value defined in local JSON files.
         * Ideally we should update the shared assessment identifiers to match what is in JSON.
         */
        private val assessmentIdentifierMap = mapOf(
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

    val viewModel: TodayViewModel by viewModel()
    lateinit var binding: FragmentTodayBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTodayBinding.inflate(layoutInflater)
        return binding.root
    }

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

    override fun onResume() {
        super.onResume()
        binding.todaysDate.text = java.time.LocalDate.now().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG))
    }

    private fun sessionsLoaded(sessions: List<ScheduledSessionWindow>) {
        binding.list.removeAllViews()
        sessions.forEach{addSession(it)}
    }

    private fun addSession(session: ScheduledSessionWindow) {
        if (session.isCompleted) return //Don't add session if it is completed

        val expirationString = session.endDateTime.toJavaLocalDateTime().format(
            DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
                .withZone(ZoneId.systemDefault()))
        val headerBinding = DueDateHeaderBinding.inflate(layoutInflater)
        headerBinding.textView.text = getString(R.string.due_on, expirationString)
        binding.list.addView(headerBinding.root)

        for (assessmentRef in session.assessments) {
            if (!assessmentRef.isCompleted) {
                val card = AssessmentCard(requireContext())
                card.setupCard(assessmentRef = assessmentRef)
                card.setOnClickListener { launchAssessment(assessmentRef) }
                binding.list.addView(card)
            }
        }
    }

    private fun launchAssessment(assessmentRef: ScheduledAssessmentReference) {
        val instanceId = assessmentRef.instanceGuid //TODO: Need instanceId for result upload -nbrown 5/4/21
        val assessmentId = assessmentIdentifierMap.get(assessmentRef.assessmentInfo.identifier) ?: assessmentRef.assessmentInfo.identifier
        val intent = Intent(requireActivity(), MtbAssessmentActivity::class.java)
        intent.putExtra(AssessmentActivity.ARG_ASSESSMENT_ID_KEY, assessmentId)
        startActivity(intent)
    }


}