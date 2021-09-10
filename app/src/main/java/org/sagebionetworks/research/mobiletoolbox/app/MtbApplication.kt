package org.sagebionetworks.research.mobiletoolbox.app

import androidx.multidex.MultiDexApplication
import edu.northwestern.mobiletoolbox.bridge.MTBKitCore
import edu.northwestern.mobiletoolbox.common.di.mtbKoinModule
import edu.northwestern.mobiletoolbox.dichotomous_engine.di.dichotomousKoinModule
import edu.northwestern.mobiletoolbox.dimensional_change_card_sort.koin.dccsKoinModule
import edu.northwestern.mobiletoolbox.flanker.di.flankerKoinModule
import edu.northwestern.mobiletoolbox.fname.koin.fnameKoinModule
import edu.northwestern.mobiletoolbox.mfs.di.mfsKoinModule
import edu.northwestern.mobiletoolbox.number_match.koin.numberMatchKoinModule
import edu.northwestern.mobiletoolbox.picture_sequence_memory.koin.psmKoinModule
import edu.northwestern.mobiletoolbox.spelling.di.spellingKoinModule
import edu.northwestern.mobiletoolbox.vocabulary.di.vocabularyKoinModule
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
            modules(
                appModule,
                mtbKoinModule,
                mfsKoinModule,
                dccsKoinModule,
                dichotomousKoinModule,
                vocabularyKoinModule,
                spellingKoinModule,
                flankerKoinModule,
                numberMatchKoinModule,
                fnameKoinModule,
                psmKoinModule
            )
        }

        MTBKitCore.boot(this)
        ScheduleNotificationsWorker.enqueueDailyScheduleNotificationWorker(this)
    }
}