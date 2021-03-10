package org.sagebionetworks.research.mtb.alpha_app

import edu.northwestern.mobiletoolbox.dimensional_change_card_sort.DccsAssessmentFragment
import edu.northwestern.mobiletoolbox.dimensional_change_card_sort.serialization.DCCSAssessmentObject
import edu.northwestern.mobiletoolbox.mfs.MfsAssessmentFragment
import edu.northwestern.mobiletoolbox.mfs.serialization.MFSAssessmentObject
import org.sagebionetworks.assessmentmodel.BranchNode
import org.sagebionetworks.assessmentmodel.presentation.AssessmentFragment
import org.sagebionetworks.assessmentmodel.presentation.AssessmentFragmentProvider

class MtbAssessmentFragmentProvider : AssessmentFragmentProvider {

    override fun fragmentFor(branchNode: BranchNode): AssessmentFragment {
        return when (branchNode) {
            is DCCSAssessmentObject -> DccsAssessmentFragment()
            is MFSAssessmentObject -> MfsAssessmentFragment()
//            is FlankerAssessmentObject -> FlankerAssessmentFragment()
//            is SpellingAssessmentObject -> SpellingAssessmentFragment()
//            is VocabularyAssessmentObject -> VocabularyAssessmentFragment()
            else -> {
                AssessmentFragment()
            }

        }
    }
}