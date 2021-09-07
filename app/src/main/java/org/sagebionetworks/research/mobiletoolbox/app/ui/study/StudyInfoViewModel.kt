package org.sagebionetworks.research.mobiletoolbox.app.ui.study

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.sagebionetworks.bridge.kmm.shared.cache.ResourceResult
import org.sagebionetworks.bridge.kmm.shared.models.Study
import org.sagebionetworks.bridge.kmm.shared.repo.AuthenticationRepository
import org.sagebionetworks.bridge.kmm.shared.repo.StudyRepo

class StudyInfoViewModel(private val studyRepo: StudyRepo,
                         private val authRepo: AuthenticationRepository) : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is study info Fragment"
    }
    val text: LiveData<String> = _text

    private val _studyLiveData = MutableLiveData<ResourceResult<Study>>()
    val studyLiveData: LiveData<ResourceResult<Study>> = _studyLiveData
    private var job: Job? = null

    val userSessionInfo = authRepo.session()

    internal fun loadStudy() {
        if (studyLiveData.value != null) {
            return
        }
        val studyId = authRepo.session()?.studyIds?.get(0)
        studyId?.let {
            job = viewModelScope.launch {
                studyRepo.getStudy(studyId).collect {
                    _studyLiveData.postValue(it)
                }
            }
        }
    }


}