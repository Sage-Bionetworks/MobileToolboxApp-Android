package org.sagebionetworks.research.mobiletoolbox.app.ui.login

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.sagebionetworks.bridge.kmm.shared.cache.ResourceResult
import org.sagebionetworks.bridge.kmm.shared.models.Study
import org.sagebionetworks.research.mobiletoolbox.app.R
import org.sagebionetworks.research.mobiletoolbox.app.databinding.FragmentWelcomeScreenBinding
import org.sagebionetworks.research.mobiletoolbox.app.recorder.model.rest.welcomeScreenData
import org.sagebionetworks.research.mobiletoolbox.app.ui.study.StudyViewModel

class WelcomeScreenFragment : Fragment() {

    companion object {
        fun newInstance() = WelcomeScreenFragment()
    }

    private val viewModel: LoginViewModel by sharedViewModel()
    private val studyViewModel: StudyViewModel by viewModel()
    private lateinit var binding: FragmentWelcomeScreenBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentWelcomeScreenBinding.inflate(inflater, container, false)

        binding.nextButton.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.container, PrivacyNoticeOnboardingFragment.newInstance())
                .addToBackStack(null)
                .commit()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //First update what we can from studyInfo, this sets default welcome message
        viewModel.studyInfo?.let { studyInfo ->
            binding.logoBackground.setBackgroundColor(
                Color.parseColor(
                    studyInfo.colorScheme?.background ?: "#F6F6F6"
                )
            )
            studyInfo.studyLogoUrl?.let {
                Glide.with(this).load(it).into(binding.logo)
            }
            binding.header.text = getString(R.string.welcome_to_study, studyInfo.name)
            binding.from.text = getString(R.string.welcome_screen_default_from, studyInfo.name)
        }

        //If we have data from study clientConfig, then use it.
        viewModel.study?.welcomeScreenData?.let { welcomeScreenData ->
            if (!welcomeScreenData.isUsingDefaultMessage) {
                binding.header.text = welcomeScreenData.welcomeScreenHeader
                binding.body.text = welcomeScreenData.welcomeScreenBody
                binding.from.text = welcomeScreenData.welcomeScreenFromText
                binding.salutation.text = welcomeScreenData.welcomeScreenSalutation
                if (!welcomeScreenData.useOptionalDisclaimer) {
                    binding.disclaimer.visibility = View.GONE
                }
            }
        }

    }

}