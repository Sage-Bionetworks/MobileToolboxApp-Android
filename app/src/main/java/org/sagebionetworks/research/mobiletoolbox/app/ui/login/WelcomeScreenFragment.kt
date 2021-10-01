package org.sagebionetworks.research.mobiletoolbox.app.ui.login

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.sagebionetworks.research.mobiletoolbox.app.R
import org.sagebionetworks.research.mobiletoolbox.app.databinding.FragmentWelcomeScreenBinding

class WelcomeScreenFragment : Fragment() {

    companion object {
        fun newInstance() = WelcomeScreenFragment()
    }

    private val viewModel: LoginViewModel by sharedViewModel()
    private lateinit var binding: FragmentWelcomeScreenBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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

        viewModel.studyInfo?.let { studyInfo ->
            binding.logoBackground.setBackgroundColor(
                Color.parseColor(
                    studyInfo.colorScheme?.background ?: "#F6F6F6"
                )
            )
            studyInfo.studyLogoUrl?.let {
                Glide.with(this).load(it).into(binding.logo);
            }
            binding.header.text = getString(R.string.welcome_to_study, studyInfo.name)
            binding.from.text = getString(R.string.welcome_screen_default_from, studyInfo.name)
        }
    }


}