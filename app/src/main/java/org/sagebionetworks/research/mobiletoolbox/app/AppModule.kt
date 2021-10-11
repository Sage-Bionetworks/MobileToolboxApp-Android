package org.sagebionetworks.research.mobiletoolbox.app

import io.ktor.client.HttpClient
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import kotlinx.serialization.json.Json
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.workmanager.dsl.worker
import org.koin.core.qualifier.StringQualifier
import org.koin.dsl.module
import org.sagebionetworks.assessmentmodel.resourcemanagement.FileLoader
import org.sagebionetworks.assessmentmodel.serialization.FileLoaderAndroid
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

    single<AssessmentResultArchiveUploader> {
        MtbAssessmentResultArchiveUploader(get(), get(), get(), get())
    }

    single { UploadRequester(get(), get()) }

    single(StringQualifier("weatherService")) {
        HttpClient {
            install(JsonFeature) {
                serializer = KotlinxSerializer(Json {
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
    viewModel { LoginViewModel(get(), get()) }

    worker { ScheduleNotificationsWorker(get(), get(), get(), get()) }
}
