package org.sagebionetworks.research.mobiletoolbox.app.arc

import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.plus
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.sagebionetworks.assessmentmodel.AssessmentRegistryProvider
import org.sagebionetworks.assessmentmodel.BranchNode
import org.sagebionetworks.assessmentmodel.presentation.AssessmentFragment
import org.sagebionetworks.assessmentmodel.presentation.AssessmentFragmentProvider
import org.sagebionetworks.assessmentmodel.serialization.EmbeddedJsonAssessmentRegistryProvider
import org.sagebionetworks.assessmentmodel.serialization.Serialization

val arcModule = module {
    single<AssessmentRegistryProvider>(named("washu-arc")) {
        EmbeddedJsonAssessmentRegistryProvider(
            get(), "washu_arc_assessment_registry",
            Json {
                serializersModule = Serialization.SerializersModule.default +
                        arcModuleInfoSerializersModule
            },
        )
    }

    single<AssessmentFragmentProvider?>(named("washu-arc")) {
        object : AssessmentFragmentProvider {
            override fun fragmentFor(branchNode: BranchNode): AssessmentFragment {
                return ArcAssessmentFragment()
            }
        }}
}