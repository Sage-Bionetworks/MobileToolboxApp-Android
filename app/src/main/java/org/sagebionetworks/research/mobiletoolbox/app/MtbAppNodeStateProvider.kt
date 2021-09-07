package org.sagebionetworks.research.mobiletoolbox.app

import edu.northwestern.mobiletoolbox.common.assessment.MtbNodeStateProvider
import edu.northwestern.mobiletoolbox.dimensional_change_card_sort.navigation.DCCSNodeStateProvider
import edu.northwestern.mobiletoolbox.dimensional_change_card_sort.serialization.DCCSAssessmentObject
import edu.northwestern.mobiletoolbox.flanker.navigation.FlankerNodeStateProvider
import edu.northwestern.mobiletoolbox.flanker.serialization.FlankerAssessmentObject
import edu.northwestern.mobiletoolbox.fname.navigation.FNAMENodeStateProvider
import edu.northwestern.mobiletoolbox.fname.serialization.FNAMEAssessmentObject
import edu.northwestern.mobiletoolbox.mfs.navigation.MfsNodeStateProvider
import edu.northwestern.mobiletoolbox.mfs.serialization.MFSAssessmentObject
import edu.northwestern.mobiletoolbox.number_match.navigation.NumberMatchNodeStateProvider
import edu.northwestern.mobiletoolbox.number_match.serialization.NumberMatchAssessmentObject
import edu.northwestern.mobiletoolbox.picture_sequence_memory.navigation.PSMNodeStateProvider
import edu.northwestern.mobiletoolbox.picture_sequence_memory.serialization.PSMAssessmentObject
import edu.northwestern.mobiletoolbox.spelling.navigation.SpellingNodeStateProvider
import edu.northwestern.mobiletoolbox.spelling.serialization.SpellingAssessmentObject
import edu.northwestern.mobiletoolbox.vocabulary.dichotomous_engine.VocabNodeStateProvider
import edu.northwestern.mobiletoolbox.vocabulary.serialization.VocabularyAssessmentObject
import org.sagebionetworks.assessmentmodel.BranchNode
import org.sagebionetworks.assessmentmodel.navigation.BranchNodeState
import org.sagebionetworks.assessmentmodel.navigation.CustomNodeStateProvider

class MtbAppNodeStateProvider(private val nodeStateProviders: List<CustomNodeStateProvider>) :
    CustomNodeStateProvider {

    override fun customBranchNodeStateFor(
        node: BranchNode,
        parent: BranchNodeState?
    ): BranchNodeState? {
        val provider = when (node) {
            is VocabularyAssessmentObject -> {
                nodeStateProviders.first { x -> x is VocabNodeStateProvider }
            }

            is SpellingAssessmentObject -> {
                nodeStateProviders.first { x -> x is SpellingNodeStateProvider }
            }

            is FlankerAssessmentObject -> {
                nodeStateProviders.first { x -> x is FlankerNodeStateProvider }
            }

            is DCCSAssessmentObject -> {
                nodeStateProviders.first { x -> x is DCCSNodeStateProvider }
            }

            is NumberMatchAssessmentObject -> {
                nodeStateProviders.first { x -> x is NumberMatchNodeStateProvider }
            }
            is MFSAssessmentObject -> {
                nodeStateProviders.first { x -> x is MfsNodeStateProvider }
            }

            is FNAMEAssessmentObject -> {
                nodeStateProviders.first { x -> x is FNAMENodeStateProvider }
            }

            is PSMAssessmentObject -> {
                nodeStateProviders.first { x -> x is PSMNodeStateProvider }
            }

            else -> {
                nodeStateProviders.first { x -> x is MtbNodeStateProvider }
            }
        }

        return provider.customBranchNodeStateFor(node, parent)
    }

}
