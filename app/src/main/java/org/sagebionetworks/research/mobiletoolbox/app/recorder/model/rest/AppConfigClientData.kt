package org.sagebionetworks.research.mobiletoolbox.app.recorder.model.rest

import kotlinx.serialization.Serializable

@Serializable
data class AppConfigClientData(
    val taskToSchemaIdentifierMap : Map<String,String> = mapOf(),
    val assessmentToTaskIdentifierMap : Map<String,String> = mapOf()
)