package org.sagebionetworks.research.mtb.alpha_app.ui.today

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.sagebionetworks.bridge.kmm.shared.cache.ResourceResult
import org.sagebionetworks.bridge.kmm.shared.repo.ActivityEventsRepo
import org.sagebionetworks.bridge.kmm.shared.repo.AuthenticationRepository
import org.sagebionetworks.bridge.kmm.shared.repo.ScheduleTimelineRepo
import org.sagebionetworks.bridge.kmm.shared.repo.ScheduledSessionWindow

class TodayViewModel(private val timelineRepo: ScheduleTimelineRepo,
                     private val authRepo: AuthenticationRepository,
                     private val activityEventsRepo: ActivityEventsRepo
                     ) : ViewModel() {
    init {
        loadTodaysSessions()
    }

    private val _sessionLiveData = MutableLiveData<ResourceResult<List<ScheduledSessionWindow>>>()
    val sessionLiveData: LiveData<ResourceResult<List<ScheduledSessionWindow>>> = _sessionLiveData

    private fun loadTodaysSessions() {
        val studyId = authRepo.session()!!.studyIds.get(0)
        viewModelScope.launch {
            val eventsResource = activityEventsRepo.getActivityEvents(studyId).firstOrNull { it is ResourceResult.Success }
            (eventsResource as? ResourceResult.Success)?.data?.let { eventList ->
                timelineRepo.getSessionsForToday(studyId, eventList).collect {
                    _sessionLiveData.postValue(it)
                }
            }
        }
    }


}

