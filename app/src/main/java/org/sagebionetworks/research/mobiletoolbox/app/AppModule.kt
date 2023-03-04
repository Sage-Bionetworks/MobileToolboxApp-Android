package org.sagebionetworks.research.mobiletoolbox.app

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.workmanager.dsl.worker
import org.koin.core.qualifier.StringQualifier
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.sagebionetworks.assessmentmodel.AssessmentRegistryProvider
import org.sagebionetworks.assessmentmodel.AssessmentResultCache
import org.sagebionetworks.assessmentmodel.BranchNode
import org.sagebionetworks.assessmentmodel.RootAssessmentRegistryProvider
import org.sagebionetworks.assessmentmodel.navigation.CustomNodeStateProvider
import org.sagebionetworks.assessmentmodel.navigation.RootCustomNodeStateProvider
import org.sagebionetworks.assessmentmodel.presentation.AssessmentFragment
import org.sagebionetworks.assessmentmodel.presentation.AssessmentFragmentProvider
import org.sagebionetworks.assessmentmodel.presentation.RootAssessmentFragmentProvider
import org.sagebionetworks.assessmentmodel.resourcemanagement.FileLoader
import org.sagebionetworks.assessmentmodel.serialization.FileLoaderAndroid
import org.sagebionetworks.bridge.assessmentmodel.survey.AssessmentResultCacheImpl
import org.sagebionetworks.bridge.assessmentmodel.survey.BridgeAssessmentRegistryProvider
import org.sagebionetworks.bridge.assessmentmodel.upload.AssessmentResultArchiveUploader
import org.sagebionetworks.bridge.kmm.shared.upload.UploadRequester
import org.sagebionetworks.research.mobiletoolbox.app.notif.ScheduleNotificationsWorker
import org.sagebionetworks.research.mobiletoolbox.app.recorder.RecorderConfigViewModel
import org.sagebionetworks.research.mobiletoolbox.app.recorder.RecorderRunner
import org.sagebionetworks.research.mobiletoolbox.app.ui.account.AccountViewModel
import org.sagebionetworks.research.mobiletoolbox.app.ui.history.HistoryViewModel
import org.sagebionetworks.research.mobiletoolbox.app.ui.login.LoginViewModel
import org.sagebionetworks.research.mobiletoolbox.app.ui.study.StudyViewModel
import org.sagebionetworks.research.mobiletoolbox.app.ui.today.TodayViewModel

val appModule = module {

    single<AssessmentResultCache> { AssessmentResultCacheImpl(get()) }
    
    factory <AssessmentResultArchiveUploader> {
        AssessmentResultArchiveUploader(get(), get(), get())
    }

    single<AssessmentRegistryProvider>() {
        RootAssessmentRegistryProvider(get(), listOf(
            get(qualifier = named("mtb-northwestern")),
            get(qualifier = named("washu-arc")),
            get(qualifier = named("sage-motorcontrol")),
            get(qualifier = named("sage-survey"))))
    }
    single<AssessmentRegistryProvider>(StringQualifier("sage-survey")) {
        BridgeAssessmentRegistryProvider(get(), get())
    }

    single<AssessmentFragmentProvider>() {
        RootAssessmentFragmentProvider(listOf(
            get(qualifier = named("mtb-northwestern")),
            get(qualifier = named("washu-arc")),
            get(qualifier = named("sage-motorcontrol")),
            get(qualifier = named("sage-survey"))))
    }
    
    single<CustomNodeStateProvider>() {
        RootCustomNodeStateProvider(listOf(
            get(qualifier = named("mtb-northwestern"))
        ))
    }

    single<AssessmentFragmentProvider?>(StringQualifier("sage-survey")) {
        object : AssessmentFragmentProvider {
            override fun fragmentFor(branchNode: BranchNode): AssessmentFragment {
                return AssessmentFragment()
            }
        }}

    single { UploadRequester(get(), get()) }

    single(StringQualifier("weatherService")) {
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }
    }
    single {
        RecorderRunner.RecorderRunnerFactory(get(), get(StringQualifier("weatherService")))
    }

    factory<FileLoader> { FileLoaderAndroid(get()) }
    viewModel { TodayViewModel(get(), get(), get(), get()) }
    viewModel { HistoryViewModel(get(), get(), get()) }
    viewModel { RecorderConfigViewModel(get(), get(), get()) }
    viewModel { StudyViewModel(get(), get()) }
    viewModel { AccountViewModel(get()) }
    viewModel { LoginViewModel(get(), get(), get()) }

    worker { ScheduleNotificationsWorker(get(), get(), get(), get()) }
}
