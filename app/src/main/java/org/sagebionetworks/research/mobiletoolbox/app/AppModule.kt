package org.sagebionetworks.research.mobiletoolbox.app

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.workmanager.dsl.worker
import org.koin.dsl.module
import org.sagebionetworks.assessmentmodel.resourcemanagement.FileLoader
import org.sagebionetworks.assessmentmodel.serialization.FileLoaderAndroid
import org.sagebionetworks.bridge.assessmentmodel.upload.AssessmentResultArchiveUploader
import org.sagebionetworks.bridge.kmm.shared.upload.UploadRequester
import org.sagebionetworks.research.mobiletoolbox.app.notif.ScheduleNotificationsWorker
import org.sagebionetworks.research.mobiletoolbox.app.ui.account.AccountViewModel
import org.sagebionetworks.research.mobiletoolbox.app.ui.history.HistoryViewModel
import org.sagebionetworks.research.mobiletoolbox.app.ui.login.LoginViewModel
import org.sagebionetworks.research.mobiletoolbox.app.ui.study.StudyInfoViewModel
import org.sagebionetworks.research.mobiletoolbox.app.ui.today.TodayViewModel

val appModule = module {

    single<AssessmentResultArchiveUploader> {
        MtbAssessmentResultArchiveUploader(get(), get(), get(), get())
    }

    single { UploadRequester(get(), get()) }

    factory<FileLoader> { FileLoaderAndroid(get()) }
    viewModel { TodayViewModel(get(), get(), get()) }
    viewModel { HistoryViewModel(get(), get(), get()) }
    viewModel { StudyInfoViewModel(get(), get()) }
    viewModel { AccountViewModel(get()) }
    viewModel { LoginViewModel(get(), get()) }

    worker {ScheduleNotificationsWorker(get(), get(), get(), get())}

}
