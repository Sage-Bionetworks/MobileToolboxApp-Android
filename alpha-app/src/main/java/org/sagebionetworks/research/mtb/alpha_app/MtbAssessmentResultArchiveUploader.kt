package org.sagebionetworks.research.mtb.alpha_app

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.joda.time.DateTime
import org.sagebionetworks.assessmentmodel.AssessmentResult
import org.sagebionetworks.bridge.data.ArchiveFile
import org.sagebionetworks.bridge.data.JsonArchiveFile
import org.sagebionetworks.bridge.assessmentmodel.upload.AssessmentResultArchiveUploader
import org.sagebionetworks.bridge.data.Archive

class MtbAssessmentResultArchiveUploader : AssessmentResultArchiveUploader() {
    val schemaVersionMap = mapOf(
        "Background_Recorders" to 6,
        "EF_DCCS" to 2,
        "EF_Flanker" to 2,
        "FNAME_TaskData" to 2,
        "MTB_Memory_For_Sequences" to 1,
        "MTB_NumberMatch" to 3,
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

        // migrate once upload utils moved to use kotlinx/java8 time - liujoshua 04/02/2021
        val endDate = DateTime.parse(assessmentResult.endDateString!!.substringBefore("["))

        return setOf(
            JsonArchiveFile(
                "taskData",
                endDate,
                resultString
            )
        )
    }
}