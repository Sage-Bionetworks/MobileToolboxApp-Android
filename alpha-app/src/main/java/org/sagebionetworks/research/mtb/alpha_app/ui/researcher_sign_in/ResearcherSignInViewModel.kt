package org.sagebionetworks.research.mtb.alpha_app.ui.researcher_sign_in

import androidx.annotation.MainThread
import androidx.annotation.NonNull
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.common.base.Strings
import org.sagebionetworks.bridge.android.access.Resource
import org.sagebionetworks.bridge.android.manager.AuthenticationManager
import org.sagebionetworks.bridge.rest.model.SharingScope
import org.sagebionetworks.bridge.rest.model.SignUp
import org.sagebionetworks.bridge.rest.model.UserSessionInfo
import org.slf4j.LoggerFactory
import rx.subscriptions.CompositeSubscription
import javax.inject.Inject

class ResearcherSignInViewModel : ViewModel {
    class Factory @Inject
    constructor(private val authenticationManager: AuthenticationManager) :
        ViewModelProvider.Factory {

        @NonNull
        override fun <T : ViewModel> create(@NonNull modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ResearcherSignInViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ResearcherSignInViewModel(authenticationManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }


    private val LOGGER = LoggerFactory.getLogger(ResearcherSignInViewModel::class.java)

    private val authenticationManager: AuthenticationManager

    private val compositeSubscription = CompositeSubscription()

    private var errorMessageMutableLiveData: MutableLiveData<String>

    private var externalId = ""

    private var firstName = ""

    private var isExternalIdValidLiveData: MutableLiveData<Boolean>

    private var isLoadingMutableLiveData: MutableLiveData<Boolean>

    private var isSignedInLiveData: MutableLiveData<Boolean>


    private val _signInStateLiveData = MutableLiveData<Resource<Boolean>>(Resource.success(false))
    val signInLiveData: LiveData<Resource<Boolean>>
        get() = _signInStateLiveData


    @MainThread
    constructor(authenticationManager: AuthenticationManager) {
        this.authenticationManager = authenticationManager

        errorMessageMutableLiveData = MutableLiveData()

        isLoadingMutableLiveData = MutableLiveData()
        isLoadingMutableLiveData.setValue(false)

        isSignedInLiveData = MutableLiveData()
        isSignedInLiveData.setValue(false)

        isExternalIdValidLiveData = MutableLiveData()
        isExternalIdValidLiveData.setValue(false)
    }

    fun doSignIn() {
        LOGGER.debug("doSignIn")

        if (Strings.isNullOrEmpty(externalId)) {
            LOGGER.warn("Cannot sign in with null or empty external Id")

            isSignedInLiveData.postValue(false)
            errorMessageMutableLiveData.postValue("Cannot sign in with null or empty external Id")
        }

        val signUp = SignUp()
        signUp.firstName(firstName)
        signUp.externalId(externalId)
        signUp.password(externalId)
        signUp.sharingScope = SharingScope.ALL_QUALIFIED_RESEARCHERS


        compositeSubscription.add(
            authenticationManager.signInWithExternalId(
                externalId,
                externalId
            ).doOnSubscribe {
                _signInStateLiveData.postValue(Resource.loading(false))
                isLoadingMutableLiveData.postValue(true)
            }.doAfterTerminate {
                isLoadingMutableLiveData.postValue(false)
            }.subscribe({ s ->
                _signInStateLiveData.postValue(Resource.success(true))
                isSignedInLiveData.postValue(true)
            }, { t ->
                // FIXME: Unknown Host exception takes a long time to return
                _signInStateLiveData.postValue((Resource.error(t.message, false)))
                isSignedInLiveData.postValue(false)
                errorMessageMutableLiveData.postValue(t.message)
            })
        )
    }

    fun getErrorMessageLiveData(): LiveData<String> {
        return errorMessageMutableLiveData
    }

    fun getIsExternalIdValid(): LiveData<Boolean> {
        return isExternalIdValidLiveData
    }

    fun getIsLoadingLiveData(): LiveData<Boolean> {
        return isLoadingMutableLiveData
    }

    fun getIsSignedInLiveData(): LiveData<Boolean> {
        return isSignedInLiveData
    }

    fun setExternalId(externalId: String?) {
        LOGGER.debug("setExternalId: {}", externalId)
        this.externalId = externalId ?: ""
        isExternalIdValidLiveData.postValue(!Strings.isNullOrEmpty(externalId))
    }

    fun setFirstName(firstName: String) {
        LOGGER.debug("setFirstName: {}", firstName)
        this.firstName = firstName
    }

    override fun onCleared() {
        compositeSubscription.unsubscribe()
    }

    internal fun onErrorMessageConsumed() {
        errorMessageMutableLiveData.postValue(null)
    }
}
