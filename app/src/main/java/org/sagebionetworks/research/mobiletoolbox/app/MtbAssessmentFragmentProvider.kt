package org.sagebionetworks.research.mobiletoolbox.app

import edu.northwestern.mobiletoolbox.dimensional_change_card_sort.DccsAssessmentFragment
import edu.northwestern.mobiletoolbox.dimensional_change_card_sort.serialization.DCCSAssessmentObject
import edu.northwestern.mobiletoolbox.flanker.FlankerAssessmentFragment
import edu.northwestern.mobiletoolbox.flanker.serialization.FlankerAssessmentObject
import edu.northwestern.mobiletoolbox.fname.FNAMEAssessmentFragment
import edu.northwestern.mobiletoolbox.fname.serialization.FNAMEAssessmentObject
import edu.northwestern.mobiletoolbox.mfs.MfsAssessmentFragment
import edu.northwestern.mobiletoolbox.mfs.serialization.MFSAssessmentObject
import edu.northwestern.mobiletoolbox.number_match.NumberMatchAssessmentFragment
import edu.northwestern.mobiletoolbox.number_match.serialization.NumberMatchAssessmentObject
import edu.northwestern.mobiletoolbox.picture_sequence_memory.PSMAssessmentFragment
import edu.northwestern.mobiletoolbox.picture_sequence_memory.serialization.PSMAssessmentObject
import edu.northwestern.mobiletoolbox.spelling.SpellingAssessmentFragment
import edu.northwestern.mobiletoolbox.spelling.serialization.SpellingAssessmentObject
import edu.northwestern.mobiletoolbox.vocabulary.VocabularyAssessmentFragment
import edu.northwestern.mobiletoolbox.vocabulary.serialization.VocabularyAssessmentObject
import org.sagebionetworks.assessmentmodel.BranchNode
import org.sagebionetworks.assessmentmodel.presentation.AssessmentFragment
import org.sagebionetworks.assessmentmodel.presentation.AssessmentFragmentProvider

class MtbAssessmentFragmentProvider : AssessmentFragmentProvider {

    override fun fragmentFor(branchNode: BranchNode): AssessmentFragment {
        return when (branchNode) {
            is DCCSAssessmentObject -> DccsAssessmentFragment()
            is MFSAssessmentObject -> MfsAssessmentFragment()
            is FlankerAssessmentObject -> FlankerAssessmentFragment()
            is SpellingAssessmentObject -> SpellingAssessmentFragment()
            is VocabularyAssessmentObject -> VocabularyAssessmentFragment()
            is NumberMatchAssessmentObject -> NumberMatchAssessmentFragment()
            is FNAMEAssessmentObject -> FNAMEAssessmentFragment()
            is PSMAssessmentObject -> PSMAssessmentFragment()
            else -> {
                AssessmentFragment()
            }
        }
    }
}