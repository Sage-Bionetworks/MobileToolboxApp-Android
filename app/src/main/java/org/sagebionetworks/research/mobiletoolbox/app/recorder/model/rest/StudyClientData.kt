package org.sagebionetworks.research.mobiletoolbox.app.recorder.model.rest

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class StudyClientData(
    val welcomeScreenData: JsonElement? = null,
    val backgroundRecorders: Map<String, Boolean?> = mapOf()
)
