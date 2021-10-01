package org.sagebionetworks.research.mobiletoolbox.app.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.sagebionetworks.bridge.kmm.shared.cache.ResourceResult
import org.sagebionetworks.bridge.kmm.shared.models.Study
import org.sagebionetworks.bridge.kmm.shared.models.StudyInfo
import org.sagebionetworks.bridge.kmm.shared.repo.AuthenticationRepository
import org.sagebionetworks.bridge.kmm.shared.repo.StudyRepo
import java.util.Locale

class LoginViewModel(val authRepo: AuthenticationRepository, val studyRepo: StudyRepo) : ViewModel() {

    private val _signInResult = MutableLiveData<SignInResult>()
    val signInResult: LiveData<SignInResult> = _signInResult

    private val _studyInfoLiveData = MutableLiveData<ResourceResult<StudyInfo>>()
    val studyInfoLiveData: LiveData<ResourceResult<StudyInfo>> = _studyInfoLiveData
    var studyInfo: StudyInfo? = null

    private val _studyLiveData = MutableLiveData<ResourceResult<Study>>()
    val studyLiveData: LiveData<ResourceResult<Study>> = _studyLiveData
    var study: Study? = null

    /**
     * Find the StudyInfo for the specified studyID. This is a public api call.
     */
    fun findStudyInfo(studyId: String) {
        viewModelScope.launch {
            val studyInfoResult = studyRepo.getStudyInfo(studyId)
            _studyInfoLiveData.postValue(studyInfoResult)
            if (studyInfoResult is ResourceResult.Success) {
                studyInfo = studyInfoResult.data
            }
        }
    }

    fun clearStudyInfo() {
        _studyInfoLiveData.value = ResourceResult.InProgress
        studyInfo = null
    }

    /**
     * Load the current study. This is an authenticated call and must be called after successfully
     * signing in.
     */
    fun loadStudy() {
        viewModelScope.launch {
            val studyId = authRepo.currentStudyId()
            studyId?.let {
                studyRepo.getStudy(studyId).collect {
                    if (it is ResourceResult.Success) {
                        study = it.data
                    }
                    _studyLiveData.postValue(it)
                }
            }
        }
    }


    fun login(externalId: String) {

        viewModelScope.launch {
            val studyId = studyInfo?.identifier ?: ""
            val externalId = "$externalId:${studyId.lowercase(Locale.US)}"
            val userSessionResult = authRepo.signInExternalId(externalId, externalId)
            if (userSessionResult is ResourceResult.Success && userSessionResult.data.authenticated == true) {
                _signInResult.value = SignInResult.Success
            } else {
                _signInResult.value = SignInResult.Failed
            }
        }
    }


    sealed class SignInResult {
        object Success : SignInResult()
        //TODO: figure out what additional info we need to pass -nbrown 02/23/2021
        object Failed : SignInResult()
    }
}