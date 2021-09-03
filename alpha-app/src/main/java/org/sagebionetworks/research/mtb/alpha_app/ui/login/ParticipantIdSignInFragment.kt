package org.sagebionetworks.research.mtb.alpha_app.ui.login

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
import org.sagebionetworks.research.mtb.alpha_app.R
import org.sagebionetworks.research.mtb.alpha_app.databinding.FragmentParticipantIdSignInBinding

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
                    binding.progressOverlay.progressOverlay.visibility = View.GONE
                    //TODO: Show welcome screen next -nbrown 8/26/2021
                    // Showing Privacy Notice for now
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.container, PrivacyNoticeOnboardingFragment.newInstance())
                        .addToBackStack(null)
                        .commit()
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