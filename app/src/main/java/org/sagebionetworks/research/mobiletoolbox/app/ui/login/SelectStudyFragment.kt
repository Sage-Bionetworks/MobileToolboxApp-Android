package org.sagebionetworks.research.mobiletoolbox.app.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.sagebionetworks.bridge.kmm.shared.cache.ResourceResult
import org.sagebionetworks.research.mobiletoolbox.app.R
import org.sagebionetworks.research.mobiletoolbox.app.databinding.FragmentSelectStudyBinding

class SelectStudyFragment : Fragment() {

    companion object {
        fun newInstance() = SelectStudyFragment()
    }

    private val viewModel: LoginViewModel by sharedViewModel()
    private lateinit var binding: FragmentSelectStudyBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSelectStudyBinding.inflate(inflater, container, false)
        binding.nextButton.setOnClickListener {
            binding.progressOverlay.progressOverlay.visibility = View.VISIBLE
            val studyId = binding.studyIdInput.text.toString()
            viewModel.findStudyInfo(studyId)
        }
        if (viewModel.studyInfo != null) {
            //Coming back from next screen
            val studyId = viewModel.studyInfo?.identifier
            binding.studyIdInput.setText(studyId)
            viewModel.clearStudyInfo()
        }
        viewModel.studyInfoLiveData.observe(viewLifecycleOwner, {
            when(it) {
                is ResourceResult.Success -> {
                    binding.studyIdInputLayout.error = null
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.container, ParticipantIdSignInFragment.newInstance())
                        .addToBackStack(null)
                        .commit()
                }
                is ResourceResult.InProgress -> {

                }
                is ResourceResult.Failed -> {
                    binding.progressOverlay.progressOverlay.visibility = View.GONE
                    binding.studyIdInputLayout.error = getString(R.string.find_study_error)
                }
            }
        })
        binding.studyIdInput.doAfterTextChanged { binding.studyIdInputLayout.error = null }

        return binding.root
    }


}