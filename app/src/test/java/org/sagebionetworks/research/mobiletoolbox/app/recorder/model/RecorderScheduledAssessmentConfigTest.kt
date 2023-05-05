package org.sagebionetworks.research.mobiletoolbox.app.recorder.model

import junit.framework.TestCase
import org.sagebionetworks.research.mobiletoolbox.app.recorder.model.rest.BackgroundRecordersConfigurationElement

class RecorderScheduledAssessmentConfigTest : TestCase() {

    private val recorderId = "recorderId"
    private val recorder = BackgroundRecordersConfigurationElement.Recorder(
        recorderId, "recorderType", listOf()
    )

    private val taskId = "taskId"
    private val otherTaskId = "taskIdOther"

    fun testIsRecorderDisabled_studyClientData() {
        var recorderConfig =
            RecorderScheduledAssessmentConfig(
                recorder,
                enabledByStudyClientData = true,
                emptySet(),
                emptyList()
            )

        assertFalse(recorderConfig.isRecorderDisabled(taskId))

        recorderConfig =
            RecorderScheduledAssessmentConfig(
                recorder,
                enabledByStudyClientData = null,
                emptySet(),
                emptyList()
            )

        assertTrue(recorderConfig.isRecorderDisabled(taskId))

        recorderConfig =
            RecorderScheduledAssessmentConfig(
                recorder,
                enabledByStudyClientData = false,
                emptySet(),
                emptyList()
            )

        assertTrue(recorderConfig.isRecorderDisabled(taskId))
    }

    fun testIsRecorderDisabled_appConfigElement() {
        val recorderConfig =
            RecorderScheduledAssessmentConfig(
                recorder,
                enabledByStudyClientData = true,
                setOf(taskId),
                emptyList()
            )

        assertTrue(recorderConfig.isRecorderDisabled(taskId))
        assertFalse(recorderConfig.isRecorderDisabled(otherTaskId))
    }

}