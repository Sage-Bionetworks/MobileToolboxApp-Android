package org.sagebionetworks.research.mobiletoolbox.app.ui.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.sagebionetworks.bridge.kmm.shared.repo.AdherenceRecordRepo
import org.sagebionetworks.bridge.kmm.shared.repo.AssessmentHistoryRecord
import org.sagebionetworks.bridge.kmm.shared.repo.AuthenticationRepository
import org.sagebionetworks.bridge.kmm.shared.repo.ScheduleTimelineRepo
import java.time.LocalDate

class HistoryViewModel(private val timelineRepo: ScheduleTimelineRepo,
                     private val authRepo: AuthenticationRepository,
                     private val adherenceRecordRepo: AdherenceRecordRepo
) : ViewModel() {
    init {
        loadTodaysSessions()
    }

    private val _sessionLiveData = MutableLiveData<List<AssessmentHistoryRecord>>()
    val sessionLiveData: LiveData<List<AssessmentHistoryRecord>> = _sessionLiveData

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
                adherenceRecordRepo.getAllCompletedCachedAssessmentAdherence(studyId).collect {
                    _sessionLiveData.postValue(it)
                }
            }
        }
    }


}