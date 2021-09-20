package org.sagebionetworks.research.mobiletoolbox.app.ui.today

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import org.sagebionetworks.bridge.kmm.shared.cache.ResourceResult
import org.sagebionetworks.bridge.kmm.shared.repo.AppConfigRepo
import org.sagebionetworks.bridge.kmm.shared.repo.AuthenticationRepository
import org.sagebionetworks.bridge.kmm.shared.repo.ScheduleTimelineRepo
import org.sagebionetworks.bridge.kmm.shared.repo.ScheduledSessionTimelineSlice
import org.sagebionetworks.research.mobiletoolbox.app.notif.ScheduleNotificationsWorker
import org.sagebionetworks.research.mobiletoolbox.app.recorder.model.rest.AppConfigClientData
import java.time.LocalDate

class TodayViewModel(
    private val timelineRepo: ScheduleTimelineRepo,
    private val authRepo: AuthenticationRepository,
    private val appConfigRepo: AppConfigRepo,
    application: Application
) : AndroidViewModel(application) {
    init {
        loadTodaysSessions()
        loadAssessmentIdentifierMap()
    }

    /**
     * Mapping from assessment ID defined in Bridge to value defined in local JSON files.
     * Ideally we should update the shared assessment identifiers to match what is in JSON.
     */
    private val assessmentIdentifierMap = mapOf(
        "vocabulary" to "Vocabulary Form 1",
        "spelling" to "MTB Spelling Form 1",
        "psm" to "Picture Sequence MemoryV1",
        "number-match" to "Number Match",
        "flanker" to "Flanker Inhibitory Control",
        "dccs" to "Dimensional Change Card Sort",
        "memory-for-sequences" to "MFS pilot 2",
        "fnamea" to "FNAME Learning Form 1",
        "fnameb" to "FNAME Test Form 1"
    )

    private val _sessionLiveData =
        MutableLiveData<Pair<String, ResourceResult<ScheduledSessionTimelineSlice>>>()
    val sessionLiveData: LiveData<Pair<String, ResourceResult<ScheduledSessionTimelineSlice>>> =
        _sessionLiveData

    private val _assessmentIdentifierMapLiveData =
        MutableLiveData(assessmentIdentifierMap)
    val assessmentIdentifierMapLiveData: LiveData<Map<String, String>> =
        _assessmentIdentifierMapLiveData

    private var timelineJob: Job? = null
    private var sessionLoadDate: LocalDate? = null

    internal fun loadTodaysSessions() {
        if (timelineJob != null && LocalDate.now().isEqual(sessionLoadDate)) {
            // Current sessions for today flow is still valid
            return
        }
        timelineJob?.cancel()
        val studyId = authRepo.session()?.studyIds?.get(0)
        studyId?.let {
            sessionLoadDate = LocalDate.now()
            timelineJob = viewModelScope.launch {
                timelineRepo.getSessionsForToday(studyId).collect {
                    _sessionLiveData.postValue(Pair(studyId, it))
                    if (it is ResourceResult.Success) {
                        ScheduleNotificationsWorker.runScheduleNotificationWorker(getApplication())
                    }
                }
            }
        }
    }

    val json = Json {
        ignoreUnknownKeys = true
    }

    internal fun loadAssessmentIdentifierMap() {
        viewModelScope.launch {
            appConfigRepo.getAppConfig().collect {
                when (it) {
                    is ResourceResult.Success ->
                        it.data.clientData?.let { clientData ->
                            return@let json.decodeFromJsonElement<AppConfigClientData>(clientData)
                        }?.run {
                            _assessmentIdentifierMapLiveData.postValue(assessmentToTaskIdentifierMap)
                        }
                    else ->
                        Log.i(
                            "TodayViewModel",
                            "Received AppConfig ResourceResult of type: ${it.javaClass.simpleName}"
                        )
                }
            }
        }
    }
}

