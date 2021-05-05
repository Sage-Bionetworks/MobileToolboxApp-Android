package org.sagebionetworks.research.mtb.alpha_app

import android.content.Context
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.joda.time.DateTimeZone
import org.sagebionetworks.assessmentmodel.AssessmentResult
import org.sagebionetworks.bridge.assessmentmodel.upload.AssessmentResultArchiveUploader
import org.sagebionetworks.bridge.data.Archive
import org.sagebionetworks.bridge.data.ArchiveFile
import org.sagebionetworks.bridge.data.JsonArchiveFile
import org.sagebionetworks.bridge.kmm.shared.upload.UploadRequester
import org.joda.time.Instant as JodaInstant
import org.sagebionetworks.bridge.kmm.shared.BridgeConfig as KmmBridgeConfig
import org.sagebionetworks.bridge.kmm.shared.repo.AuthenticationRepository

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

    // maybe we can pull from AppConfig? -liujoshua 04/02/2021
    override fun getArchiveBuilderForActivity(assessmentResult: AssessmentResult): Archive.Builder {
        val schema = assessmentResult.schemaIdentifier
        return Archive.Builder.forActivity(schema, schemaVersionMap[schema] ?: 1)
    }

    override fun toArchiveFiles(
        assessmentResult: AssessmentResult,
        jsonCoder: Json
    ): Set<ArchiveFile> {
        val resultString = jsonCoder.encodeToString(assessmentResult)

        val kotlinEndTimeInstant = assessmentResult.endDateTime!!
        val jodaEndTime = JodaInstant(kotlinEndTimeInstant.toEpochMilliseconds())
            .toDateTime(DateTimeZone.UTC)

        return setOf(
            JsonArchiveFile(
                "taskData",
                jodaEndTime,
                resultString
            )
        )
    }
}