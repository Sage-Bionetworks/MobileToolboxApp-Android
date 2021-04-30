package org.sagebionetworks.research.mtb.alpha_app

import androidx.multidex.MultiDexApplication
import edu.northwestern.mobiletoolbox.common.di.mtbKoinModule
import edu.northwestern.mobiletoolbox.mfs.di.mfsKoinModule
import edu.northwestern.mobiletoolbox.mtbnavigation.kit.MTBKitCore
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.component.KoinComponent
import org.sagebionetworks.bridge.kmm.presentation.di.presentationModule
import org.sagebionetworks.bridge.kmm.shared.di.*


class MtbAlphaApplication : MultiDexApplication(), KoinComponent {

    override fun onCreate() {
        super.onCreate()

        initKoin (enableNetworkLogs = BuildConfig.DEBUG){
            androidLogger()
            androidContext(this@MtbAlphaApplication)
            workManagerFactory()
            modules(presentationModule)
            modules(appModule, mtbKoinModule, mfsKoinModule)//, dichotomousKoinModule, vocabularyKoinModule, spellingKoinModule, flankerKoinModule)

        }
        MTBKitCore.boot(this)

    }
}