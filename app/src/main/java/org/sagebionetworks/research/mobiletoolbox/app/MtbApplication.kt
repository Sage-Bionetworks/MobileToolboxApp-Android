package org.sagebionetworks.research.mobiletoolbox.app

import androidx.multidex.MultiDexApplication
import co.touchlab.kermit.Logger
import co.touchlab.kermit.crashlytics.CrashlyticsLogWriter
import edu.northwestern.mobiletoolbox.assessments_provider.mtbModules
import edu.northwestern.mobiletoolbox.bridge.MTBKitCore
import edu.wustl.arc.sageassessments.SageArcApplication
import edu.wustl.arc.sageassessments.arcModule
import net.danlew.android.joda.JodaTimeAndroid
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.logger.Level
import org.sagebionetworks.bridge.kmm.shared.di.*
import org.sagebionetworks.bridge.kmm.shared.upload.UploadRequester
import org.sagebionetworks.motorControlModule
import org.sagebionetworks.research.mobiletoolbox.app.notif.ScheduleNotificationsWorker


class MtbApplication : MultiDexApplication(), KoinComponent {

    override fun onCreate() {
        // TODO remove Joda and juse kotlinx/java8 time - liujoshua 04/02/2021
        JodaTimeAndroid.init(this)

        super.onCreate()

        Logger.addLogWriter(CrashlyticsLogWriter())

        initKoin (enableNetworkLogs = BuildConfig.DEBUG){
            androidLogger(Level.ERROR)
            androidContext(this@MtbApplication)
            workManagerFactory()
            modules(appModule)
            modules(motorControlModule)
            modules(mtbModules())
            modules(arcModule)
        }

        MTBKitCore.boot(this)
        ScheduleNotificationsWorker.enqueueDailyScheduleNotificationWorker(this)
        //Trigger an upload worker to process any failed uploads
        val uploadRequester: UploadRequester = get()
        uploadRequester.queueUploadWorker()

        // WashU Arc library setup
        SageArcApplication.setupDefaultConfig(this)
    }
}