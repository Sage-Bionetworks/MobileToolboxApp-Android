package org.sagebionetworks.research.mtb.alpha_app

import edu.northwestern.mobiletoolbox.common.assessment.MtbNodeStateProvider
import edu.northwestern.mobiletoolbox.dimensional_change_card_sort.navigation.DCCSNodeStateProvider
import edu.northwestern.mobiletoolbox.dimensional_change_card_sort.serialization.DCCSAssessmentObject
//import edu.northwestern.mobiletoolbox.flanker.navigation.FlankerNodeStateProvider
//import edu.northwestern.mobiletoolbox.flanker.serialization.FlankerAssessmentObject
//import edu.northwestern.mobiletoolbox.spelling.navigation.SpellingNodeStateProvider
//import edu.northwestern.mobiletoolbox.spelling.serialization.SpellingAssessmentObject
//import edu.northwestern.mobiletoolbox.vocabulary.dichotomous_engine.VocabNodeStateProvider
//import edu.northwestern.mobiletoolbox.vocabulary.serialization.VocabularyAssessmentObject
import org.sagebionetworks.assessmentmodel.BranchNode
import org.sagebionetworks.assessmentmodel.navigation.BranchNodeState
import org.sagebionetworks.assessmentmodel.navigation.CustomNodeStateProvider

class MtbAppNodeStateProvider(private val nodeStateProviders: List<CustomNodeStateProvider>) : CustomNodeStateProvider {

    override fun customBranchNodeStateFor(node: BranchNode, parent: BranchNodeState?): BranchNodeState? {
        val provider = when (node) {
//            is VocabularyAssessmentObject -> {
//                nodeStateProviders.first { x -> x is VocabNodeStateProvider }
//            }
//T
//            is SpellingAssessmentObject -> {
//                nodeStateProviders.first { x -> x is SpellingNodeStateProvider }
//            }
//
//            is FlankerAssessmentObject -> {
//                nodeStateProviders.first { x -> x is FlankerNodeStateProvider }
//            }

            is DCCSAssessmentObject -> {
                nodeStateProviders.first { x -> x is DCCSNodeStateProvider}
            }
            else -> {
                nodeStateProviders.first { x -> x is MtbNodeStateProvider }
            }
        }

        return provider.customBranchNodeStateFor(node, parent)
    }

}
