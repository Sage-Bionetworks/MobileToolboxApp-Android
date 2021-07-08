package org.sagebionetworks.research.mtb.alpha_app.ui.today

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.sagebionetworks.bridge.kmm.shared.cache.ResourceResult
import org.sagebionetworks.bridge.kmm.shared.repo.ActivityEventsRepo
import org.sagebionetworks.bridge.kmm.shared.repo.AuthenticationRepository
import org.sagebionetworks.bridge.kmm.shared.repo.ScheduleTimelineRepo
import org.sagebionetworks.bridge.kmm.shared.repo.ScheduledSessionTimelineSlice
import org.sagebionetworks.bridge.kmm.shared.repo.ScheduledSessionWindow
import java.time.LocalDate

class TodayViewModel(private val timelineRepo: ScheduleTimelineRepo,
                     private val authRepo: AuthenticationRepository,
                     private val activityEventsRepo: ActivityEventsRepo
                     ) : ViewModel() {
    init {
        loadTodaysSessions()
    }

    private val _sessionLiveData = MutableLiveData<ResourceResult<ScheduledSessionTimelineSlice>>()
    val sessionLiveData: LiveData<ResourceResult<ScheduledSessionTimelineSlice>> = _sessionLiveData

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
                    _sessionLiveData.postValue(it)
                }
            }
        }
    }


}

