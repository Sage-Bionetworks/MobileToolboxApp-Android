package org.sagebionetworks.research.mobiletoolbox.app.recorder.model.rest

import junit.framework.TestCase
import kotlinx.serialization.decodeFromString
import org.sagebionetworks.research.mobiletoolbox.app.recorder.model.recorderConfigJsonCoder

class AppConfigClientDataTest : TestCase() {

    fun testAppConfigClientData() {
        val json = "{\n" +
                "  \"taskToSchemaIdentifierMap\": {\n" +
                "    \"MTB Spelling Form 1\": \"mtbSpelling\",\n" +
                "    \"FNAME Test Form 1\": \"FNAME_TaskData\",\n" +
                "    \"FNAME Learning Form 1\": \"FNAME_TaskData\",\n" +
                "    \"FNAME Test Form 2\": \"FNAME_TaskData\",\n" +
                "    \"FNAME Learning Form 2\": \"FNAME_TaskData\",\n" +
                "    \"Flanker Inhibitory Control\": \"EF_Flanker\",\n" +
                "    \"MFS pilot 2\": \"MTB_Memory_For_Sequences\",\n" +
                "    \"Dimensional Change Card Sort\": \"EF_DCCS\",\n" +
                "    \"Number Match\": \"MTB_NumberMatch\",\n" +
                "    \"Picture Sequence MemoryV1\": \"MTB_Picture_Sequence_Memory\",\n" +
                "    \"Vocabulary Form 1\": \"mtbVocab\",\n" +
                "    \"Vocabulary Form 2\": \"mtbVocab\"\n" +
                "  },\n" +
                "  \"assessmentToTaskIdentifierMap\": {\n" +
                "    \"memory-for-sequences\": \"MFS pilot 2\",\n" +
                "    \"dccs\": \"Dimensional Change Card Sort\",\n" +
                "    \"fnamea\": \"FNAME Test Form 1\",\n" +
                "    \"fnameb\": \"FNAME Learning Form 1\",\n" +
                "    \"flanker\": \"Flanker Inhibitory Control\",\n" +
                "    \"number-match\": \"Number Match\",\n" +
                "    \"psm\": \"Picture Sequence MemoryV1\",\n" +
                "    \"spelling\": \"MTB Spelling Form 1\",\n" +
                "    \"vocabulary\": \"Vocabulary Form 1\"\n" +
                "  }\n" +
                "}"

        val result = recorderConfigJsonCoder.decodeFromString<AppConfigClientData>(json)

        assertNotNull(result.assessmentToTaskIdentifierMap)

        assertEquals("Dimensional Change Card Sort", result.assessmentToTaskIdentifierMap["dccs"])

        assertNotNull(result.taskToSchemaIdentifierMap)

        assertEquals("mtbSpelling", result.taskToSchemaIdentifierMap["MTB Spelling Form 1"])
    }

    fun testAppConfigClientData_empty() {
        val json = "{}"

        val result = recorderConfigJsonCoder.decodeFromString<AppConfigClientData>(json)
        assertNotNull(result)
        assertTrue(result.taskToSchemaIdentifierMap.isEmpty())
        assertTrue(result.assessmentToTaskIdentifierMap.isEmpty())
    }
}