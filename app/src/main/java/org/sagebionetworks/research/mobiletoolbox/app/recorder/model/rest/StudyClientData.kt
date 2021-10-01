package org.sagebionetworks.research.mobiletoolbox.app.recorder.model.rest

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

//TODO: This is only used by tests. Update StudyClientDataTest and remove. -nbrown 10/1/2021
@Serializable
data class StudyClientData(
    val welcomeScreenData: JsonElement? = null,
    val backgroundRecorders: Map<String, Boolean?> = mapOf()
)
