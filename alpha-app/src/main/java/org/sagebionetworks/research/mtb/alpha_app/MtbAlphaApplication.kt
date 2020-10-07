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
import org.sagebionetworks.bridge.android.BridgeApplication
import org.sagebionetworks.bridge.android.di.BridgeStudyComponent
import org.sagebionetworks.research.mtb.alpha_app.di.DaggerMtbAlphaAppComponent
import org.sagebionetworks.research.mtb.alpha_app.di.DaggerMtbAlphaResearcherScopeComponent
import org.sagebionetworks.research.mtb.alpha_app.di.MtbAlphaResearcherScopeComponent
import javax.inject.Inject


class MtbAlphaApplication : BridgeApplication(), HasSupportFragmentInjector,
        HasActivityInjector, HasServiceInjector {
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
}