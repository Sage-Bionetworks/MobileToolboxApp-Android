package org.sagebionetworks.research.mtb.alpha_app

import android.app.Activity
import android.app.Service
import androidx.fragment.app.Fragment
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import dagger.android.HasServiceInjector
import dagger.android.support.DaggerApplication
import dagger.android.support.HasSupportFragmentInjector
import edu.northwestern.mobiletoolbox.common.di.mtbKoinModule
import edu.northwestern.mobiletoolbox.mfs.di.mfsKoinModule
import edu.northwestern.mobiletoolbox.mtbnavigation.kit.MTBKitCore
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.component.KoinComponent
import org.sagebionetworks.bridge.android.BridgeApplication
import org.sagebionetworks.bridge.android.di.BridgeStudyComponent
import org.sagebionetworks.bridge.kmm.presentation.di.presentationModule
import org.sagebionetworks.research.mtb.alpha_app.di.DaggerMtbAlphaAppComponent
import org.sagebionetworks.research.mtb.alpha_app.di.DaggerMtbAlphaResearcherScopeComponent
import org.sagebionetworks.research.mtb.alpha_app.di.MtbAlphaResearcherScopeComponent
import org.sagebionetworks.bridge.kmm.shared.di.*
import javax.inject.Inject


class MtbAlphaApplication : BridgeApplication(), HasSupportFragmentInjector,
        HasActivityInjector, HasServiceInjector, KoinComponent {
    @Inject
    protected lateinit var dispatchingActivityInjector: DispatchingAndroidInjector<Activity>

    @Inject
    protected lateinit var dispatchingSupportFragmentInjector: DispatchingAndroidInjector<Fragment>

    @Inject
    protected lateinit var dispatchingServiceInjector: DispatchingAndroidInjector<Service>

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerMtbAlphaAppComponent.builder()
                .application(this)
                .researcherScopeComponent(orInitBridgeManagerProvider as MtbAlphaResearcherScopeComponent)
                .build()
    }

    override fun initBridgeManagerScopedComponent(bridgeStudyComponent: BridgeStudyComponent): MtbAlphaResearcherScopeComponent {
        return DaggerMtbAlphaResearcherScopeComponent.builder()
                .applicationContext(applicationContext)
                .bridgeStudyComponent(bridgeStudyComponent)
                .build()
    }

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