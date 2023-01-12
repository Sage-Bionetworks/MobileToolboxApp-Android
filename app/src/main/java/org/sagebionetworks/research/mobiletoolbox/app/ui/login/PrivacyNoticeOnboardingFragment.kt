package org.sagebionetworks.research.mobiletoolbox.app.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import org.sagebionetworks.assessmentmodel.presentation.compose.BottomNavigation
import org.sagebionetworks.research.mobiletoolbox.app.R
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

        binding.composeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                BottomNavigation(
                    { onPrevClicked() },
                    { onNextClicked() },
                    nextEnabled = true
                )
            }
        }

        return binding.root
    }

    private fun onNextClicked() {
        val privacyNoticeFragment = childFragmentManager.findFragmentById(R.id.fragment_container_view) as PrivacyNoticeFragment
        if (!privacyNoticeFragment.goNext()) {
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, PermissionsFragment.newInstance())
                .addToBackStack(null)
                .commit()
        }
    }

    private fun onPrevClicked() {
        val privacyNoticeFragment = childFragmentManager.findFragmentById(R.id.fragment_container_view) as PrivacyNoticeFragment
        if (!privacyNoticeFragment.goPrev()) {
            parentFragmentManager.popBackStack()
        }
    }

    companion object {

        @JvmStatic
        fun newInstance() = PrivacyNoticeOnboardingFragment()
    }
}