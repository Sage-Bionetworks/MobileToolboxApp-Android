package org.sagebionetworks.research.mobiletoolbox.app.recorder.model.rest

import junit.framework.TestCase
import kotlinx.serialization.decodeFromString
import org.sagebionetworks.research.mobiletoolbox.app.recorder.model.recorderConfigJsonCoder

class StudyClientDataTest : TestCase() {


    fun testDecodeBackgroundRecorders() {
        val json = "{\n" +
                "  \"welcomeScreenData\": {\n" +
                "    \"welcomeScreenHeader\": \"\",\n" +
                "    \"welcomeScreenBody\": \"We’re grateful that you could join us and help us better understand sleep and cognition.\\n\\nThe study should only take a few minutes out of your day over the next two weeks.\\n\\nPlease feel free to reach out if you have any questions / concerns.\\n\\nThank you for your time,\",\n" +
                "    \"welcomeScreenFromText\": \"Research team\",\n" +
                "    \"welcomeScreenSalutation\": \"Welcome to the Sleep and Cognition study!\",\n" +
                "    \"useOptionalDisclaimer\": true,\n" +
                "    \"isUsingDefaultMessage\": false\n" +
                "  },\n" +
                "  \"backgroundRecorders\": {\n" +
                "    \"motion\": false,\n" +
                "    \"microphone\": null\n" +
                "  },\n" +
                "  \"events\": [\n" +
                "    {\n" +
                "      \"label\": \"test123\",\n" +
                "      \"identifier\": \"132153995\",\n" +
                "      \"updateType\": \"mutable\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"label\": \"tret\",\n" +
                "      \"identifier\": \"557669233\",\n" +
                "      \"updateType\": \"mutable\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"generateIds\": false\n" +
                "}"
        val result = recorderConfigJsonCoder.decodeFromString<StudyClientData>(json)

        with(result) {
            assertNotNull(welcomeScreenData)
            assertEquals(false, backgroundRecorders["motion"])
            assertNull(backgroundRecorders["microphone"])
        }
    }

    fun testDecodeEmpty() {
        val json = "{}"
        val result = recorderConfigJsonCoder.decodeFromString<StudyClientData>(json)

        with(result) {
            assertNull(welcomeScreenData)
            assertTrue(backgroundRecorders.isEmpty())
        }
    }
}