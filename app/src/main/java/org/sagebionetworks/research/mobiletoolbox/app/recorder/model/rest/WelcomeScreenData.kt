package org.sagebionetworks.research.mobiletoolbox.app.recorder.model.rest

import android.util.Log
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import org.sagebionetworks.bridge.kmm.shared.models.Study

@Serializable
data class WelcomeScreenData(
    val welcomeScreenHeader: String? = null,
    val welcomeScreenBody: String? = null,
    val welcomeScreenFromText: String? = null,
    val welcomeScreenSalutation: String? = null,
    val useOptionalDisclaimer: Boolean = true,
    val isUsingDefaultMessage: Boolean = true)

val Study.welcomeScreenData: WelcomeScreenData?
    get() = clientData?.let { jsonElement ->

        try {
            jsonElement.jsonObject["welcomeScreenData"]?.let {
                Json.decodeFromJsonElement<WelcomeScreenData>(it)
            }
        } catch (e: Exception) {
            Log.w("Study", "Failed to decode WelcomeScreenData", e)
            null
        }
    }

