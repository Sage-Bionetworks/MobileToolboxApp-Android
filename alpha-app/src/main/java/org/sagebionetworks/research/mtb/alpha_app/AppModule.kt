package org.sagebionetworks.research.mtb.alpha_app

import edu.northwestern.mobiletoolbox.common.assessment.MtbNodeStateProvider
import edu.northwestern.mobiletoolbox.dimensional_change_card_sort.navigation.DCCSNodeStateProvider
import edu.northwestern.mobiletoolbox.dimensional_change_card_sort.serialization.dccsModuleInfoSerializersModule
import edu.northwestern.mobiletoolbox.flanker.navigation.FlankerNodeStateProvider
import edu.northwestern.mobiletoolbox.flanker.serialization.flankerModuleInfoSerializersModule
import edu.northwestern.mobiletoolbox.fname.navigation.FNAMENodeStateProvider
import edu.northwestern.mobiletoolbox.fname.serialization.fnameModuleInfoSerializersModule
import edu.northwestern.mobiletoolbox.mfs.navigation.MfsNodeStateProvider
import edu.northwestern.mobiletoolbox.mfs.serialization.mfsModuleInfoSerializersModule
import edu.northwestern.mobiletoolbox.number_match.navigation.NumberMatchNodeStateProvider
import edu.northwestern.mobiletoolbox.number_match.serialization.numberMatchModuleInfoSerializersModule
import edu.northwestern.mobiletoolbox.picture_sequence_memory.navigation.PSMNodeStateProvider
import edu.northwestern.mobiletoolbox.picture_sequence_memory.serialization.psmModuleInfoSerializersModule
import edu.northwestern.mobiletoolbox.spelling.navigation.SpellingNodeStateProvider
import edu.northwestern.mobiletoolbox.spelling.serialization.spellingModuleInfoSerializersModule
import edu.northwestern.mobiletoolbox.vocabulary.dichotomous_engine.VocabNodeStateProvider
import edu.northwestern.mobiletoolbox.vocabulary.serialization.vocabularyModuleInfoSerializersModule
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.plus
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import org.sagebionetworks.assessmentmodel.AssessmentRegistryProvider
import org.sagebionetworks.assessmentmodel.navigation.CustomNodeStateProvider
import org.sagebionetworks.assessmentmodel.presentation.AssessmentFragmentProvider
import org.sagebionetworks.assessmentmodel.resourcemanagement.FileLoader
import org.sagebionetworks.assessmentmodel.serialization.EmbeddedJsonAssessmentRegistryProvider
import org.sagebionetworks.assessmentmodel.serialization.FileLoaderAndroid
import org.sagebionetworks.assessmentmodel.serialization.nodeSerializersModule
import org.sagebionetworks.research.mtb.alpha_app.ui.today.TodayViewModel
import org.sagebionetworks.bridge.assessmentmodel.upload.AssessmentResultArchiveUploader
import org.sagebionetworks.bridge.kmm.shared.upload.UploadRequester
import org.sagebionetworks.research.mtb.alpha_app.ui.history.HistoryViewModel

val appModule = module {

    single<AssessmentRegistryProvider> {
        EmbeddedJsonAssessmentRegistryProvider(
            get(), "embedded_assessment_registry",
            Json {
                serializersModule = nodeSerializersModule +
                        dccsModuleInfoSerializersModule +
                        flankerModuleInfoSerializersModule +
                        mfsModuleInfoSerializersModule +
                        spellingModuleInfoSerializersModule +
                        vocabularyModuleInfoSerializersModule +
                        fnameModuleInfoSerializersModule +
                        numberMatchModuleInfoSerializersModule +
                        psmModuleInfoSerializersModule
                isLenient = true
            },
        )
    }

    single<AssessmentResultArchiveUploader> {
        MtbAssessmentResultArchiveUploader(get(), get(), get(), get())
    }

    single { UploadRequester(get(), get()) }

    single<AssessmentFragmentProvider> { MtbAssessmentFragmentProvider() }
    single<CustomNodeStateProvider> {
        MtbAppNodeStateProvider(
            listOf(
                VocabNodeStateProvider(get()),
                FlankerNodeStateProvider(get()),
                SpellingNodeStateProvider(get()),
                DCCSNodeStateProvider(get()),
                NumberMatchNodeStateProvider(get()),
                MfsNodeStateProvider(get()),
                FNAMENodeStateProvider(get()),
                PSMNodeStateProvider(get()),
                MtbNodeStateProvider(get()),
            )
        )
    }

    factory<FileLoader> { FileLoaderAndroid(get()) }
    viewModel { TodayViewModel(get(), get(), get()) }
    viewModel { HistoryViewModel(get(), get(), get()) }

}
