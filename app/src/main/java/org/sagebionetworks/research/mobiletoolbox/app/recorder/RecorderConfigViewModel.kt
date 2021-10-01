package org.sagebionetworks.research.mobiletoolbox.app.recorder

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import org.sagebionetworks.bridge.kmm.shared.cache.ResourceResult
import org.sagebionetworks.bridge.kmm.shared.models.Study
import org.sagebionetworks.bridge.kmm.shared.repo.AppConfigRepo
import org.sagebionetworks.bridge.kmm.shared.repo.AuthenticationRepository
import org.sagebionetworks.bridge.kmm.shared.repo.StudyRepo
import org.sagebionetworks.bridge.mpp.network.generated.models.AppConfig
import org.sagebionetworks.research.mobiletoolbox.app.recorder.model.RecorderScheduledAssessmentConfig
import org.sagebionetworks.research.mobiletoolbox.app.recorder.model.rest.BackgroundRecordersConfigurationElement
import org.sagebionetworks.research.mobiletoolbox.app.recorder.model.rest.StudyClientData

class RecorderConfigViewModel(
    private val authRepo: AuthenticationRepository,
    private val studyRepo: StudyRepo,
    private val appConfigRepo: AppConfigRepo
) : ViewModel() {
    val tag = this.javaClass.canonicalName
    val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    init {
        loadRecorderConfigs()
    }

    private val _recorderScheduledAssessmentConfigLiveData =
        MutableLiveData<List<RecorderScheduledAssessmentConfig>>()
    val recorderScheduledAssessmentConfig: LiveData<List<RecorderScheduledAssessmentConfig>> =
        _recorderScheduledAssessmentConfigLiveData

    internal fun loadRecorderConfigs() {
        viewModelScope.launch {
            getAppRecorderConfig().combine(getStudyRecorderConfig()) { appRecorderConfig, studyRecorderConfig ->
                if (appRecorderConfig == null || studyRecorderConfig == null) {
                    // when we don't have both parts of the recorder config, return no config
                    Log.w(
                        tag, "Returning empty recorder configs: " +
                                "${appRecorderConfig ?: "Missing app config "}" +
                                "${studyRecorderConfig ?: "Missing study config"}"
                    )
                    _recorderScheduledAssessmentConfigLiveData.postValue(
                        listOf()
                    )
                } else {
                    val recorderScheduledAssessmentConfig =
                        appRecorderConfig.recorders.map { recorderConfig ->
                            recorderConfig.identifier to recorderConfig
                        }
                            .toMap()
                            .mapValues { recorderConfigEntry ->
                                RecorderScheduledAssessmentConfig(
                                    recorderConfigEntry.value,
                                    studyRecorderConfig[recorderConfigEntry.key],
                                    appRecorderConfig.excludeMapping[recorderConfigEntry.key]?.toSet()
                                        ?: setOf()
                                )
                            }

                    _recorderScheduledAssessmentConfigLiveData.postValue(
                        recorderScheduledAssessmentConfig.values.toList()
                    )
                }
            }
        }
    }

    /**
     * @return successfully retrieved configuration or null
     */
    fun getAppRecorderConfig(): Flow<BackgroundRecordersConfigurationElement?> {
        return appConfigRepo.getAppConfig().map { appConfig ->
            return@map when (appConfig) {
                is ResourceResult.Success<AppConfig> ->
                    appConfig.data.configElements?.get("BackgroundRecorders")?.let {
                        json.decodeFromJsonElement<BackgroundRecordersConfigurationElement>(it)
                    }
                else ->
                    null
            }
        }
    }

    /**
     * @return successfully retrieved configuration or null
     */
    fun getStudyRecorderConfig(): Flow<Map<String, Boolean?>?> {

        return authRepo.currentStudyId().let { studyId ->
            if (studyId == null) {
                return listOf<Map<String, Boolean?>?>(null).asFlow()
            }
            return@let studyRepo.getStudy(studyId).map {
                return@map when (it) {
                    is ResourceResult.Success<Study> ->
                        it.data.backgroundRecorders ?: mapOf()
                    else ->
                        null
                }
            }
        }

    }
}

val Study.backgroundRecorders: Map<String, Boolean?>?
    get() = clientData?.let { jsonElement ->

        try {
            jsonElement.jsonObject["backgroundRecorders"]?.let {
                Json.decodeFromJsonElement<Map<String, Boolean?>>(it)
            }
        } catch (e: Exception) {
            Log.w("Study", "Failed to decode Study background recorders", e)
            null
        }
    }