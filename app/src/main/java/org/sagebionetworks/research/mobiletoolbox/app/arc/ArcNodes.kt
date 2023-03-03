package org.sagebionetworks.research.mobiletoolbox.app.arc

import com.google.gson.GsonBuilder
import edu.wustl.arc.api.models.TestSubmission
import edu.wustl.arc.core.Device
import edu.wustl.arc.study.TestSession
import edu.wustl.arc.time.TimeUtil
import edu.wustl.arc.utilities.VersionUtil
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
import org.sagebionetworks.assessmentmodel.InterruptionHandlingObject
import org.sagebionetworks.assessmentmodel.JsonArchivableFile
import org.sagebionetworks.assessmentmodel.JsonFileArchivableResult
import org.sagebionetworks.assessmentmodel.ModuleInfo
import org.sagebionetworks.assessmentmodel.Node
import org.sagebionetworks.assessmentmodel.Result
import org.sagebionetworks.assessmentmodel.navigation.BranchNodeState
import org.sagebionetworks.assessmentmodel.navigation.Navigator
import org.sagebionetworks.assessmentmodel.serialization.NodeContainerObject
import org.sagebionetworks.assessmentmodel.serialization.StepObject

val arcNodeSerializersModule = SerializersModule {
    polymorphic(Node::class) {
        subclass(ArcTestInfoStepObject::class)
        subclass(ArcTestBeginStepObject::class)
        subclass(ArcSymbolTestStepObject::class)
    }
    polymorphic(Assessment::class) {
        subclass(ArcAssessmentObject::class)
    }
    polymorphic(Result::class) {
        subclass(ArcAssessmentResultObject::class)
    }
}

@Serializable
enum class ArcAssessmentType {
    @SerialName("symbol_test")
    SYMBOLS,
    @SerialName("price_test")
    PRICES,
    @SerialName("grid_test")
    GRIDS;

    fun toIdentifier(): String {
        return when(this) {
            SYMBOLS -> "symbols"
            PRICES -> "prices"
            GRIDS -> "grids"
        }
    }
}

@Serializable
@SerialName("arcAssessmentObject")
data class ArcAssessmentObject(
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

    companion object {
        val gson = GsonBuilder().setPrettyPrinting().create()

        fun createAssessmentResult(session: TestSession,
                                   arcTestResultObject: ArcAssessmentResultObject,
                                   participantId: String): ArcAssessmentResultObject {

            val test = TestSubmission()
            test.app_version = VersionUtil.getAppVersionName()
            test.device_id = Device.getId()
            test.device_info = Device.getInfo()
            test.participant_id = participantId
            test.session_id = java.lang.String.valueOf(session.id)
            test.id = test.session_id
            test.tests = session.copyOfTestData
            test.timezone_name = TimeUtil.getTimezoneName()
            test.timezone_offset = TimeUtil.getTimezoneOffset()

            // TODO: mdephillips 3/2/23 Are these fields relevant anymore?
//            test.session_date = TimeUtil.toUtcDouble(session.scheduledTime)
//            if (session.startTime != null) {
//                test.start_time = TimeUtil.toUtcDouble(session.startTime)
//            }
//            test.day = session.dayIndex
//            test.week = weeks
//            test.session = session.index
//            test.missed_session = if (session.wasMissed()) 1 else 0
//            test.finished_session = if (session.wasFinished()) 1 else 0

            val jsonStr = gson.toJson(test)

            return arcTestResultObject.copy(
                endDateTime = Clock.System.now(),
                resultJsonStr = jsonStr)
        }
    }

    override fun createNavigator(nodeState: BranchNodeState): Navigator {
        return super.createNavigator(nodeState)
    }

    override fun createResult(): AssessmentResult = super<Assessment>.createResult()
    override fun unpack(originalNode: Node?,
                        moduleInfo: ModuleInfo,
                        registryProvider: AssessmentRegistryProvider): ArcAssessmentObject {
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
    var testType: ArcAssessmentType
) : StepObject()

/**
 * Step where user can get prepared right before they begin a test
 */
@Serializable
@SerialName("testBegin")
data class ArcTestBeginStepObject(
    override val identifier: String,
    override var imageInfo: ImageInfo? = null
) : StepObject()

/**
 * Step where user participates in the Symbols Test and the resulting data is collected
 */
@Serializable
@SerialName("symbolsTest")
data class ArcSymbolTestStepObject(
    override val identifier: String,
    override var imageInfo: ImageInfo? = null
) : StepObject()

@Serializable
@SerialName("arcResult")
data class ArcAssessmentResultObject(
    override val identifier: String,
    override var startDateTime: Instant = Clock.System.now(),
    override var endDateTime: Instant? = null,
    var isComplete: Boolean = false,
    var assessmentType: ArcAssessmentType,
    var resultJsonStr: String? = null,
) : JsonFileArchivableResult {

    override fun copyResult(identifier: String): ArcAssessmentResultObject {
        return this.copy()
    }

    override fun getJsonArchivableFile(stepPath: String): JsonArchivableFile {
        return JsonArchivableFile(
            filename = "data.json",
            json = Json.encodeToString(resultJsonStr),
            // TODO: mdephillips 3/2/23 how do I add ARC JSON schemas to git here?
            jsonSchema = ""//"https://sage-bionetworks.github.io/mobile-client-json/schemas/v1/TappingResultObject.json"
        )
    }
}

