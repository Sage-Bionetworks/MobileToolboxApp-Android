package org.sagebionetworks.research.mobiletoolbox.app.ui.login

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.sagebionetworks.bridge.kmm.shared.cache.ResourceResult
import org.sagebionetworks.research.mobiletoolbox.app.R
import org.sagebionetworks.research.mobiletoolbox.app.databinding.FragmentParticipantIdSignInBinding
import org.sagebionetworks.research.mobiletoolbox.app.notif.ScheduleNotificationsWorker

class ParticipantIdSignInFragment : Fragment() {

    companion object {
        fun newInstance() = ParticipantIdSignInFragment()
    }

    private val viewModel: LoginViewModel by sharedViewModel()
    private lateinit var binding: FragmentParticipantIdSignInBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentParticipantIdSignInBinding.inflate(inflater, container, false)
        binding.prevButton.setOnClickListener {
            goBack()
        }
        binding.nextButton.setOnClickListener {
            binding.progressOverlay.progressOverlay.visibility = View.VISIBLE
            viewModel.login(binding.participantIdInput.text.toString())
        }
        viewModel.signInResult.observe(viewLifecycleOwner, Observer {
            when(it) {
                is LoginViewModel.SignInResult.Success -> {
                    binding.participantIdInputLayout.error = null
                    loadStudy()
                    ScheduleNotificationsWorker.runScheduleNotificationWorker(requireContext())
                }
                is LoginViewModel.SignInResult.Failed -> {
                    binding.progressOverlay.progressOverlay.visibility = View.GONE
                    binding.participantIdInputLayout.error = getString(R.string.participant_login_error)
                }
            }
        })
        binding.participantIdInput.doAfterTextChanged { binding.participantIdInputLayout.error = null }

        return binding.root
    }

    private fun loadStudy() {
        //Since we are already showing user a loading spinner, go ahead and load study so
        //it is ready for welcome screen and privacy screens.
        viewModel.studyLiveData.observe(viewLifecycleOwner, Observer {
            when(it) {
                is ResourceResult.Success -> {
                    goToNextScreen()
                }
                is ResourceResult.InProgress -> {
                    // Wait for a success or failure
                }
                is ResourceResult.Failed -> {
                    //Failed to load Study, go to next screen anyways. We will show default values.
                    goToNextScreen()
                }
            }

        })
        viewModel.loadStudy()
    }

    private fun goToNextScreen() {
        binding.progressOverlay.progressOverlay.visibility = View.GONE
        parentFragmentManager.beginTransaction()
            .replace(R.id.container, WelcomeScreenFragment.newInstance())
            .addToBackStack(null)
            .commit()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val studyInfo = viewModel.studyInfo
        if (studyInfo == null) {
            goBack()
        } else {
            binding.logoBackground.setBackgroundColor(Color.parseColor(studyInfo.colorScheme?.background ?: "#FFFFFF"))
            studyInfo.studyLogoUrl?.let {
                Glide.with(this).load(it).into(binding.logo);
            }
            binding.studyName.text = getString(R.string.welcome_to_study, studyInfo.name)
            binding.studyId.text = getString(R.string.study_id, studyInfo.identifier)
        }

    }

    private fun goBack() {
        parentFragmentManager.popBackStack()
    }

}