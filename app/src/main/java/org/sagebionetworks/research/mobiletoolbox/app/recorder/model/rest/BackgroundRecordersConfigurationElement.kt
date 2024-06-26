package org.sagebionetworks.research.mobiletoolbox.app.recorder.model.rest

import kotlinx.serialization.Serializable
import org.sagebionetworks.assessmentmodel.passivedata.recorder.weather.WeatherServiceProviderName

@Serializable
data class BackgroundRecordersConfigurationElement(
    val recorders: List<Recorder>,
    val excludeMapping: Map<String, List<String>>
) {
    @Serializable
    data class Recorder(
        val identifier: String,
        val type: String,
        val services: List<RecorderService>? = listOf()
    )

    @Serializable
    data class RecorderService(
        val identifier: String,
        val type: String,
        val provider: WeatherServiceProviderName,
        val key: String
    )
}