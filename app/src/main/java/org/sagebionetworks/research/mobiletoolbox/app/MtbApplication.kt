package org.sagebionetworks.research.mobiletoolbox.app

import androidx.multidex.MultiDexApplication
import edu.northwestern.mobiletoolbox.assessments_provider.mtbModules
import edu.northwestern.mobiletoolbox.bridge.MTBKitCore
import net.danlew.android.joda.JodaTimeAndroid
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.component.KoinComponent
import org.koin.core.logger.Level
import org.sagebionetworks.bridge.kmm.presentation.di.presentationModule
import org.sagebionetworks.bridge.kmm.shared.di.*
import org.sagebionetworks.research.mobiletoolbox.app.notif.AlarmReceiver
import org.sagebionetworks.research.mobiletoolbox.app.notif.ScheduleNotificationsWorker


class MtbApplication : MultiDexApplication(), KoinComponent {

    override fun onCreate() {
        // TODO remove Joda and juse kotlinx/java8 time - liujoshua 04/02/2021
        JodaTimeAndroid.init(this)

        super.onCreate()

        initKoin (enableNetworkLogs = BuildConfig.DEBUG){
            androidLogger(Level.ERROR)
            androidContext(this@MtbApplication)
            workManagerFactory()
            modules(presentationModule)
            modules(appModule)
            modules(mtbModules())
        }

        MTBKitCore.boot(this)
        ScheduleNotificationsWorker.enqueueDailyScheduleNotificationWorker(this)
    }
}