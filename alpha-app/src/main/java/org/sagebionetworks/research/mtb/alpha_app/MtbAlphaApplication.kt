package org.sagebionetworks.research.mtb.alpha_app

import androidx.multidex.MultiDexApplication
import edu.northwestern.mobiletoolbox.common.di.mtbKoinModule
import edu.northwestern.mobiletoolbox.dichotomous_engine.di.dichotomousKoinModule
import edu.northwestern.mobiletoolbox.flanker.di.flankerKoinModule
import edu.northwestern.mobiletoolbox.fname.koin.fnameKoinModule
import edu.northwestern.mobiletoolbox.mfs.di.mfsKoinModule
import edu.northwestern.mobiletoolbox.mtbnavigation.kit.MTBKitCore
import edu.northwestern.mobiletoolbox.number_match.koin.numberMatchKoinModule
import edu.northwestern.mobiletoolbox.picture_sequence_memory.koin.psmKoinModule
import edu.northwestern.mobiletoolbox.spelling.di.spellingKoinModule
import edu.northwestern.mobiletoolbox.vocabulary.di.vocabularyKoinModule
import net.danlew.android.joda.JodaTimeAndroid
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.KoinExperimentalAPI
import org.koin.core.component.KoinComponent
import org.sagebionetworks.bridge.kmm.presentation.di.presentationModule
import org.sagebionetworks.bridge.kmm.shared.di.*


class MtbAlphaApplication : MultiDexApplication(), KoinComponent {

    override fun onCreate() {
        // TODO remove Joda and juse kotlinx/java8 time - liujoshua 04/02/2021
        JodaTimeAndroid.init(this)

        super.onCreate()

        initKoin(enableNetworkLogs = BuildConfig.DEBUG) {
            androidLogger()
            androidContext(this@MtbAlphaApplication)
            workManagerFactory()
            modules(presentationModule)
            modules(
                appModule,
                mtbKoinModule,
                mfsKoinModule,
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
    }
}