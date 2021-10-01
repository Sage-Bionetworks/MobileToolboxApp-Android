package org.sagebionetworks.research.mobiletoolbox.app.recorder.model

import junit.framework.TestCase
import org.sagebionetworks.research.mobiletoolbox.app.recorder.model.rest.BackgroundRecordersConfigurationElement

class RecorderScheduledAssessmentConfigTest : TestCase() {

    val recorderId = "recorderId"
    val recorder = BackgroundRecordersConfigurationElement.Recorder(
        recorderId, "recorderType", listOf()
    )

    val taskId = "taskId"
    val otherTaskId = "taskIdOther"

    fun testIsRecorderDisabled_studyClientData() {
        var recorderConfig =
            RecorderScheduledAssessmentConfig(
                recorder,
                enabledByStudyClientData = true,
                setOf()
            )

        assertFalse(recorderConfig.isRecorderDisabled(taskId))

        recorderConfig =
            RecorderScheduledAssessmentConfig(
                recorder,
                enabledByStudyClientData = null,
                setOf()
            )

        assertFalse(recorderConfig.isRecorderDisabled(taskId))

        recorderConfig =
            RecorderScheduledAssessmentConfig(
                recorder,
                enabledByStudyClientData = false,
                setOf()
            )

        assertTrue(recorderConfig.isRecorderDisabled(taskId))
    }

    fun testIsRecorderDisabled_appConfigElement() {
        var recorderConfig =
            RecorderScheduledAssessmentConfig(
                recorder,
                enabledByStudyClientData = true,
                setOf(taskId)
            )

        assertTrue(recorderConfig.isRecorderDisabled(taskId))
        assertFalse(recorderConfig.isRecorderDisabled(otherTaskId))
    }

}