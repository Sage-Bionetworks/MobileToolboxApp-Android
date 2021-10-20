package org.sagebionetworks.research.mobiletoolbox.app

import android.content.Context
import com.google.common.io.Files
import io.github.aakira.napier.Napier
import kotlinx.datetime.Instant
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.sagebionetworks.assessmentmodel.AssessmentResult
import org.sagebionetworks.assessmentmodel.passivedata.ResultData
import org.sagebionetworks.assessmentmodel.passivedata.recorder.FileResult
import org.sagebionetworks.assessmentmodel.passivedata.recorder.weather.WeatherResult
import org.sagebionetworks.bridge.assessmentmodel.upload.AssessmentResultArchiveUploader
import org.sagebionetworks.bridge.data.Archive
import org.sagebionetworks.bridge.data.ArchiveFile
import org.sagebionetworks.bridge.data.ByteSourceArchiveFile
import org.sagebionetworks.bridge.data.JsonArchiveFile
import org.sagebionetworks.bridge.kmm.shared.repo.AuthenticationRepository
import org.sagebionetworks.bridge.kmm.shared.upload.UploadRequester
import java.io.File
import org.joda.time.Instant as JodaInstant
import org.sagebionetworks.bridge.kmm.shared.BridgeConfig as KmmBridgeConfig


@ExperimentalSerializationApi
class MtbAssessmentResultArchiveUploader(
    context: Context,
    bridgeConfig: KmmBridgeConfig,
    uploadRequester: UploadRequester,
    authenticationRepository: AuthenticationRepository
) : AssessmentResultArchiveUploader(
    context,
    bridgeConfig,
    uploadRequester,
    authenticationRepository
) {
    val tag = " MtbAssessmentResultArchiveUploader"

    // TODO : pull from AppConfig - liujoshua 2021-09-25
    val schemaVersionMap = mapOf(
        "Background_Recorders" to 6,
        "EF_DCCS" to 2,
        "EF_Flanker" to 2,
        "FNAME_TaskData" to 2,
        "mtbSpelling" to 2,
        "mtbVocab" to 2,
        "MTB_Memory_For_Sequences" to 1,
        "MTB_NumberMatch" to 3,
        "MTB_Picture_Sequence_Memory" to 2,
        "SpellingCalibration" to 3
    )

    // TODO: do not rely on consumer class setting our state - liujoshua 2021-10-04
    val asyncResults: MutableSet<ResultData> = mutableSetOf()
    private val asyncResultJsonCoder = Json {
        serializersModule = SerializersModule {
            polymorphic(ResultData::class) {
                subclass(WeatherResult::class)
            }
        }
        explicitNulls = false
    }

    internal fun getAsyncRecordArchiveFiles(): Set<ArchiveFile> {
        Napier.i("Archiving asyncResults ${asyncResults.map { "${it.identifier}, " }}")
        return asyncResults.flatMap { asyncResult ->
            convertAsyncResultToArchiveFile(asyncResult)
        }.toSet()
    }

    // maybe we can pull from AppConfig? -liujoshua 04/02/2021
    override fun getArchiveBuilderForActivity(assessmentResult: AssessmentResult): Archive.Builder {
        val schema = assessmentResult.schemaIdentifier
        return Archive.Builder.forActivity(schema, schemaVersionMap[schema] ?: 1)
    }

    fun convertAsyncResultToArchiveFile(resultData: ResultData): Set<ArchiveFile> {
        Napier.i("Converting and archiving ${resultData.identifier} result")
        if (resultData is FileResult) {

            val file: File = File(resultData.relativePath)
            if (!file.isFile) {
                Napier.w("No file found at relative path, skipping file result: $resultData")
                return emptySet()
            }

            return setOf(
                ByteSourceArchiveFile(
                    file.name, resultData.endDate.toJodaDateTime(),
                    Files.asByteSource(file)
                )
            )

        } else {
            with(resultData) {
                return setOf(
                    JsonArchiveFile(
                        "$identifier.json",
                        endDate.toJodaDateTime(),
                        asyncResultJsonCoder.encodeToString(this)
                    )
                )
            }
        }
    }

    override fun toArchiveFiles(
        assessmentResult: AssessmentResult,
        jsonCoder: Json
    ): Set<ArchiveFile> {
        Napier.d("Writing result for assessment ${assessmentResult.identifier}")
        val resultString = jsonCoder.encodeToString(assessmentResult)

        val kotlinEndTimeInstant = assessmentResult.endDateTime!!
        val jodaEndTime = kotlinEndTimeInstant.toJodaDateTime()

        return setOf(
            JsonArchiveFile(
                "taskData.json",
                jodaEndTime,
                resultString
            ),
            JsonArchiveFile(
                "taskData.json",
                jodaEndTime,
                resultString
            )
        ).plus(getAsyncRecordArchiveFiles())
    }
}

fun Instant.toJodaDateTime(): DateTime = JodaInstant(this.toEpochMilliseconds())
    .toDateTime(DateTimeZone.UTC)