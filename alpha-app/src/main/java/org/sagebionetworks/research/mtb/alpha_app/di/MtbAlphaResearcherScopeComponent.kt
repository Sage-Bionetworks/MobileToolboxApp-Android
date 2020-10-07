package org.sagebionetworks.research.mtb.alpha_app.di

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import org.sagebionetworks.bridge.android.di.BridgeStudyComponent
import org.sagebionetworks.bridge.android.di.BridgeStudyParticipantScope
import org.sagebionetworks.bridge.android.manager.BridgeManagerProvider

@Component(modules = [], dependencies = [BridgeStudyComponent::class])
@BridgeStudyParticipantScope
interface MtbAlphaResearcherScopeComponent : BridgeManagerProvider {
    @Component.Builder
    interface Builder : BridgeManagerProvider.Builder {
        @BindsInstance
        override fun applicationContext(context: Context): Builder
        override fun bridgeStudyComponent(bridgeStudyComponent: BridgeStudyComponent): Builder
        override fun build(): MtbAlphaResearcherScopeComponent
    }
}