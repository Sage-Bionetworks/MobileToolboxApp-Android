package org.sagebionetworks.research.mobiletoolbox.app.arc

import android.content.Context
import androidx.fragment.app.Fragment
import edu.wustl.arc.path_data.GridTestPathData
import edu.wustl.arc.path_data.SymbolsTestPathData
import edu.wustl.arc.paths.templates.TestInfoTemplate
import edu.wustl.arc.paths.tests.SymbolTest
import edu.wustl.arc.paths.tests.TestBegin
import edu.wustl.arc.study.Study
import edu.wustl.arc.utilities.ViewUtil
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.sagebionetworks.assessmentmodel.Assessment
import org.sagebionetworks.assessmentmodel.AssessmentRegistryProvider
import org.sagebionetworks.assessmentmodel.AssessmentResult
import org.sagebionetworks.assessmentmodel.AsyncActionConfiguration
import org.sagebionetworks.assessmentmodel.AsyncActionContainer
import org.sagebionetworks.assessmentmodel.ImageInfo
import org.sagebionetworks.assessmentmodel.InstructionStep
import org.sagebionetworks.assessmentmodel.InterruptionHandlingObject
import org.sagebionetworks.assessmentmodel.JsonArchivableFile
import org.sagebionetworks.assessmentmodel.JsonFileArchivableResult
import org.sagebionetworks.assessmentmodel.ModuleInfo
import org.sagebionetworks.assessmentmodel.Node
import org.sagebionetworks.assessmentmodel.Result
import org.sagebionetworks.assessmentmodel.Step
import org.sagebionetworks.assessmentmodel.navigation.BranchNodeState
import org.sagebionetworks.assessmentmodel.navigation.Navigator
import org.sagebionetworks.assessmentmodel.serialization.NodeContainerObject
import org.sagebionetworks.assessmentmodel.serialization.StepObject
import org.sagebionetworks.research.mobiletoolbox.app.R
import org.sagebionetworks.research.mobiletoolbox.app.arc.ArcTestInfoStepObject.Companion.createTestInfoFragment

val arcNodeSerializersModule = SerializersModule {
    polymorphic(Node::class) {
        subclass(ArcTestInfoStepObject::class)
        subclass(ArcTestBeginStepObject::class)
        subclass(ArcSymbolTestStepObject::class)
    }
    polymorphic(Assessment::class) {
        subclass(ArcSymbolAssessmentObject::class)
    }
    polymorphic(Result::class) {
        subclass(ArcSymbolTestResultObject::class)
    }
}


interface ArcContextCreatable<out T> {
    fun createFromContext(context: Context): T
}


interface ArcStepFragmentCreatable {
    fun createFromStep(context: Context, step: Step): Fragment?
}

@Serializable
@SerialName("arcSymbolAssessmentObject")
data class ArcSymbolAssessmentObject(
    override val identifier: String,
    override val guid: String? = null,
    override val versionString: String? = null,
    override val schemaIdentifier: String? = null,
    override var estimatedMinutes: Int = 0,
    @SerialName("asyncActions")
    override val asyncActions: List<AsyncActionConfiguration> = listOf(),
    override val copyright: String? = null,
    @SerialName("\$schema")
    override val schema: String? = null,
    override val interruptionHandling: InterruptionHandlingObject = InterruptionHandlingObject(),
    @SerialName("steps")
    override val children: List<Node> = listOf()
) : NodeContainerObject(), Assessment, AsyncActionContainer {

    companion object: ArcStepFragmentCreatable {

        override fun createFromStep(context: Context, step: Step): Fragment? {
            return when (step) {
                is ArcStepFragmentCreatable -> step.createFromStep(context, step)
                else -> return null
            }
        }

        fun createFromContext(context: Context, guid: String, schemaId: String): ArcSymbolAssessmentObject {
            val id = "symbol_test"

            val children = listOf<Node>(
                ArcTestInfoStepObject.createFromContext(context),
                ArcTestBeginStepObject.createFromContext(context),
                ArcSymbolTestStepObject.createFromContext(context))

            return ArcSymbolAssessmentObject(
                identifier = id,
                guid = guid,
                schemaIdentifier = schemaId,
                children = children)
        }
    }

    override fun createNavigator(nodeState: BranchNodeState): Navigator {
        return super.createNavigator(nodeState)
    }

    override fun createResult(): AssessmentResult = super<Assessment>.createResult()
    override fun unpack(originalNode: Node?,
                        moduleInfo: ModuleInfo,
                        registryProvider: AssessmentRegistryProvider): ArcSymbolAssessmentObject {
        super<Assessment>.unpack(originalNode, moduleInfo, registryProvider)
        val copyChildren = children.map {
            it.unpack(null, moduleInfo, registryProvider)
        }
        val identifier = originalNode?.identifier ?: this.identifier
        val guid = originalNode?.identifier ?: this.guid
        val copy = copy(identifier = identifier, guid = guid, children = copyChildren)
        copy.copyFrom(this)
        return copy
    }
}

/**
 * Step that shows information about the test
 */
@Serializable
@SerialName("testInfo")
data class ArcTestInfoStepObject(
    override val identifier: String,
    @SerialName("image")
    override var imageInfo: ImageInfo? = null,
    var buttonText: String? = null,
    var type: String? = null
) : StepObject() {

    companion object: ArcContextCreatable<ArcTestInfoStepObject> {

        override fun createFromContext(context: Context): ArcTestInfoStepObject {
            val step = ArcTestInfoStepObject(
                identifier = "intro",
                buttonText = ViewUtil.getHtmlString(R.string.button_begintest),
                type = "symbols")
            step.title = ViewUtil.getHtmlString(edu.wustl.arc.assessments.R.string.symbols_header)
            step.subtitle = ViewUtil.getHtmlString(edu.wustl.arc.assessments.R.string.symbols_body)
            return step
        }

        fun createTestInfoFragment(step: ArcTestInfoStepObject): TestInfoTemplate {
            val data = when(step.type) {
                "symbol" -> SymbolsTestPathData()
                else -> GridTestPathData()
            }
            Study.setCurrentSegmentData(data)

            return TestInfoTemplate(
                step.detail,
                step.title,
                step.subtitle,
                step.type,
                step.buttonText)
        }
    }
}

/**
 * Step where user can get prepared right before they begin a test
 */
@Serializable
@SerialName("testBegin")
data class ArcTestBeginStepObject(
    override val identifier: String,
    override var imageInfo: ImageInfo? = null
) : StepObject() {

    companion object: ArcContextCreatable<ArcTestBeginStepObject>, ArcStepFragmentCreatable {

        override fun createFromStep(context: Context, step: Step): Fragment? {
            return TestBegin()
        }

        override fun createFromContext(context: Context): ArcTestBeginStepObject {
            return ArcTestBeginStepObject("test_begin")
        }
    }
}

/**
 * Step where user participates in the Symbols Test and the resulting data is collected
 */
@Serializable
@SerialName("symbolTest")
data class ArcSymbolTestStepObject(
    override val identifier: String,
    override var imageInfo: ImageInfo? = null
) : StepObject() {

    companion object: ArcContextCreatable<ArcSymbolTestStepObject>, ArcStepFragmentCreatable {

        override fun createFromStep(context: Context, step: Step): Fragment? {
            return SymbolTest()
        }

        override fun createFromContext(context: Context): ArcSymbolTestStepObject {
            return ArcSymbolTestStepObject("symbols_test")
        }
    }
}

@Serializable
@SerialName("symbol")
data class ArcSymbolTestResultObject(
    override val identifier: String,
    override var startDateTime: Instant = Clock.System.now(),
    override var endDateTime: Instant? = null
) : JsonFileArchivableResult {
    override fun copyResult(identifier: String): ArcSymbolTestResultObject {
        return this.copy()
    }

    override fun getJsonArchivableFile(stepPath: String): JsonArchivableFile {
        return JsonArchivableFile(
            filename = "symbol_test",
            json = Json.encodeToString(this),
            jsonSchema = ""//"https://sage-bionetworks.github.io/mobile-client-json/schemas/v1/TappingResultObject.json"
        )
    }
}

