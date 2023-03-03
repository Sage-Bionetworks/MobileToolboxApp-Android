package org.sagebionetworks.research.mobiletoolbox.app

import android.content.res.Configuration
import androidx.multidex.MultiDexApplication
import co.touchlab.kermit.Logger
import co.touchlab.kermit.crashlytics.CrashlyticsLogWriter
import edu.northwestern.mobiletoolbox.assessments_provider.mtbModules
import edu.northwestern.mobiletoolbox.bridge.MTBKitCore
import edu.wustl.Arc.app.arc.model.ArcStateMachine
import edu.wustl.arc.core.ArcApplication
import edu.wustl.arc.core.Config
import edu.wustl.arc.study.Study
import edu.wustl.arc.study.TestVariant
import edu.wustl.arc.ui.BottomNavigationView
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
import org.sagebionetworks.research.mobiletoolbox.app.arc.arcModule
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
        arcOnCreate()
    }

    private fun arcOnCreate() {
        BottomNavigationView.shouldShowEarnings = false
        Config.CHOOSE_LOCALE = false
        Config.CHECK_CONTACT_INFO = true
        Config.CHECK_SESSION_INFO = true
        Config.CHECK_PROGRESS_INFO = true
        Config.ENABLE_VIGNETTES = true
        Config.IS_REMOTE = true
        Config.ENABLE_SIGNATURES = true
        Config.USE_HELP_SCREEN = false
        Config.TEST_VARIANT_GRID = TestVariant.Grid.V2
        Config.TEST_VARIANT_PRICE = TestVariant.Price.Original

        // Initialize library
        ArcApplication.initialize(this) {
            Study.getInstance().registerStateMachine(
                ArcStateMachine::class.java
            )
        }
        Study.getStateMachine().initialize()
        ArcApplication.getInstance().localeOptions = ArrayList()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        ArcApplication.getInstance()?.onConfigurationChanged(newConfig)
    }
}