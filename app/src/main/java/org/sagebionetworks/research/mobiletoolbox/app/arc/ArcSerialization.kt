package org.sagebionetworks.research.mobiletoolbox.app.arc

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.sagebionetworks.assessmentmodel.Assessment
import org.sagebionetworks.assessmentmodel.AssessmentPlaceholder
import org.sagebionetworks.assessmentmodel.EmbeddedJsonModuleInfo
import org.sagebionetworks.assessmentmodel.JsonModuleInfo
import org.sagebionetworks.assessmentmodel.TransformableAssessment
import org.sagebionetworks.assessmentmodel.resourcemanagement.ResourceInfo
import org.sagebionetworks.assessmentmodel.serialization.Serialization

val arcModuleInfoSerializersModule = SerializersModule {
    polymorphic(JsonModuleInfo::class) {
        subclass(ArcModuleInfoObject::class)
    }
}

@Serializable
@SerialName("ArcModuleInfoObject")
data class ArcModuleInfoObject(
    override val assessments: List<Assessment>,
    override var packageName: String? = null,
    override val bundleIdentifier: String? = null): ResourceInfo, EmbeddedJsonModuleInfo {

    override val resourceInfo: ResourceInfo
        get() = this
    override val jsonCoder: Json
        get() {
            return Json {
                serializersModule = arcNodeSerializersModule +
                        Serialization.SerializersModule.default
                ignoreUnknownKeys = true
                isLenient = true
            }
        }

    @Transient
    override var decoderBundle: Any? = null
}