package org.sagebionetworks.research.mtb.alpha_app.di

import android.app.Application
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import org.sagebionetworks.bridge.android.di.BridgeApplicationScope
import org.sagebionetworks.research.mtb.alpha_app.MtbAlphaApplication


@Component(modules = [MtbAlphaAppModule::class, AndroidInjectionModule::class], dependencies = [MtbAlphaResearcherScopeComponent::class])
@BridgeApplicationScope
interface MtbAlphaAppComponent : AndroidInjector<MtbAlphaApplication> {
    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application?): Builder
        fun researcherScopeComponent(researcherScopeComponent: MtbAlphaResearcherScopeComponent?): Builder
        fun build(): MtbAlphaAppComponent
    }
}