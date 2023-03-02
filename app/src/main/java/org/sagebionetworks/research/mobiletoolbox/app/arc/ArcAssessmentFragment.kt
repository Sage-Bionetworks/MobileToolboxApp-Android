//
//  ArcAssessmentFragment.kt
//

package org.sagebionetworks.research.mobiletoolbox.app.arc

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import org.sagebionetworks.assessmentmodel.*
import org.sagebionetworks.assessmentmodel.presentation.AssessmentFragment

class ArcAssessmentFragment: AssessmentFragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Prevent landscape mode for all steps in Motor Control Assessments
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    override fun getFragmentForStep(step: Step): Fragment {
        // Check for ARC related step, if something goes wrong or
        // the fragment can't be created for some reason, null is returned
        (step as? ArcStepFragmentCreatable)?.let {
            it.createFromStep(requireContext(), step)?.let { fragment ->
                return fragment
            }
        }
        return super.getFragmentForStep(step)
    }
}