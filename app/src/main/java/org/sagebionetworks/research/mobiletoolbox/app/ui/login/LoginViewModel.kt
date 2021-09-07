package org.sagebionetworks.research.mobiletoolbox.app.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import org.sagebionetworks.bridge.kmm.shared.cache.ResourceResult
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

    fun findStudy(studyId: String) {
        viewModelScope.launch {
            val studyInfoResult = studyRepo.getStudyInfo(studyId)
            _studyInfoLiveData.postValue(studyInfoResult)
            if (studyInfoResult is ResourceResult.Success) {
                studyInfo = studyInfoResult.data
            }
        }
    }

    fun clearStudy() {
        _studyInfoLiveData.value = null
        studyInfo = null
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