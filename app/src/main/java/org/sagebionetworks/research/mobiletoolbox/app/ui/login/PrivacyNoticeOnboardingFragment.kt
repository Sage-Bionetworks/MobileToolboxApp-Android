package org.sagebionetworks.research.mobiletoolbox.app.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import org.sagebionetworks.research.mobiletoolbox.app.databinding.FragmentPrivacyNoticeOnboardingBinding
import org.sagebionetworks.research.mobiletoolbox.app.ui.study.PrivacyNoticeFragment

class PrivacyNoticeOnboardingFragment : Fragment() {

    private lateinit var binding: FragmentPrivacyNoticeOnboardingBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentPrivacyNoticeOnboardingBinding.inflate(inflater, container, false)
        binding.nextButton.setOnClickListener {
            onNextClicked()
        }
        binding.prevButton.setOnClickListener {
            onPrevClicked()
        }

        return binding.root
    }

    private fun onNextClicked() {
        val privacyNoticeFragment = childFragmentManager.findFragmentByTag("privacy_notice") as PrivacyNoticeFragment
        if (!privacyNoticeFragment.goNext()) {
            requireActivity().setResult(AppCompatActivity.RESULT_OK)
            requireActivity().finish()
        }
    }

    private fun onPrevClicked() {
        val privacyNoticeFragment = childFragmentManager.findFragmentByTag("privacy_notice") as PrivacyNoticeFragment
        privacyNoticeFragment.goPrev()
    }

    companion object {

        @JvmStatic
        fun newInstance() = PrivacyNoticeOnboardingFragment()
    }
}