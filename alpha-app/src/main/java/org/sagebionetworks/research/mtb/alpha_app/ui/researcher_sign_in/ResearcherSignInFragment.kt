package org.sagebionetworks.research.mtb.alpha_app.ui.researcher_sign_in

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_researcher_sign_in.externalId
import kotlinx.android.synthetic.main.fragment_researcher_sign_in.progressBar
import kotlinx.android.synthetic.main.fragment_researcher_sign_in.signIn
import kotlinx.android.synthetic.main.fragment_researcher_sign_in.signInErrorText
import org.sagebionetworks.bridge.android.access.BridgeAccessViewModel
import org.sagebionetworks.bridge.android.access.Resource
import org.sagebionetworks.research.mtb.alpha_app.R
import javax.inject.Inject

class ResearcherSignInFragment : DaggerFragment() {

    companion object {
        fun newInstance() =
            ResearcherSignInFragment()
    }

    @Inject
    lateinit var researchSignInViewModelFactory: ResearcherSignInViewModel.Factory
    val researcherSignInViewModel by viewModels<ResearcherSignInViewModel> {
        researchSignInViewModelFactory
    }

    @Inject
    lateinit var bridgeAccessViewModelFactory: BridgeAccessViewModel.Factory
    val bridgeAccessViewModel by activityViewModels<BridgeAccessViewModel> {
        bridgeAccessViewModelFactory
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_researcher_sign_in, container, false)
    }

    override fun onStart() {
        super.onStart()

        externalId.doOnTextChanged { text, start, count, after ->
            researcherSignInViewModel.setExternalId(text.toString())
        }

        signIn.setOnClickListener {
            researcherSignInViewModel.doSignIn()
            signInErrorText.visibility = GONE
        }

//        researcherSignInViewModel.getIsLoadingLiveData()
//            .observe(this, Observer { handleSignInProgress(it) })
//        researcherSignInViewModel.getIsSignedInLiveData().observe(this, Observer { isSignedIn ->
//            if (isSignedIn) {
//                bridgeAccessViewModel.checkAccess()
//            }
//        })
        researcherSignInViewModel.signInLiveData.observe(this, Observer { isSingedInResource ->
            when (isSingedInResource.status) {
                Resource.Status.LOADING -> {
                    progressBar.visibility = VISIBLE
                    signInErrorText.visibility = GONE
                }
                Resource.Status.ERROR -> {
                    signInErrorText.visibility = VISIBLE
                    progressBar.visibility = GONE
                    signInErrorText.text = isSingedInResource.message
                }
                Resource.Status.SUCCESS -> {
                    progressBar.visibility = GONE
                    signInErrorText.visibility = GONE

                    if (isSingedInResource.data == true) {
                        // Manual call required because bridgeAccessViewModel doesn't listen to updates yet
                        bridgeAccessViewModel.checkAccess()
                    }
                }

            }
        })
    }
}


