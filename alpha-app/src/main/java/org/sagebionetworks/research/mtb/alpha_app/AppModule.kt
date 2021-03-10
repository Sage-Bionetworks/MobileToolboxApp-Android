package org.sagebionetworks.research.mtb.alpha_app

import edu.northwestern.mobiletoolbox.common.assessment.MtbNodeStateProvider
import edu.northwestern.mobiletoolbox.dimensional_change_card_sort.navigation.DCCSNodeStateProvider
import edu.northwestern.mobiletoolbox.dimensional_change_card_sort.serialization.dccsModuleInfoSerializersModule
import edu.northwestern.mobiletoolbox.mfs.serialization.mfsModuleInfoSerializersModule
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.plus
import org.koin.dsl.module
import org.sagebionetworks.assessmentmodel.AssessmentRegistryProvider
import org.sagebionetworks.assessmentmodel.navigation.CustomNodeStateProvider
import org.sagebionetworks.assessmentmodel.presentation.AssessmentFragmentProvider
import org.sagebionetworks.assessmentmodel.resourcemanagement.FileLoader
import org.sagebionetworks.assessmentmodel.serialization.EmbeddedJsonAssessmentRegistryProvider
import org.sagebionetworks.assessmentmodel.serialization.FileLoaderAndroid
import org.sagebionetworks.assessmentmodel.serialization.nodeSerializersModule

val appModule = module {

    single<AssessmentRegistryProvider> {
        EmbeddedJsonAssessmentRegistryProvider(
                get(), "embedded_assessment_registry",
                Json {
                    serializersModule = nodeSerializersModule +
                            dccsModuleInfoSerializersModule +
                            //flankerModuleInfoSerializersModule +
                            mfsModuleInfoSerializersModule
//                            spellingModuleInfoSerializersModule +
//                            vocabularyModuleInfoSerializersModule
                },
        )
    }

    single<AssessmentFragmentProvider> { MtbAssessmentFragmentProvider() }
    single<CustomNodeStateProvider> {
        MtbAppNodeStateProvider(listOf(
//                VocabNodeStateProvider(get()),
//                FlankerNodeStateProvider(get()),
//                SpellingNodeStateProvider(get()),
                DCCSNodeStateProvider(get()),
                MtbNodeStateProvider(get())
        ))
    }

    factory<FileLoader> { FileLoaderAndroid(get()) }

}
