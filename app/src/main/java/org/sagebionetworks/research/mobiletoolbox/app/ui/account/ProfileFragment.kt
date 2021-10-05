package org.sagebionetworks.research.mobiletoolbox.app.ui.account

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.sagebionetworks.bridge.kmm.shared.repo.AuthenticationRepository
import org.sagebionetworks.research.mobiletoolbox.app.BuildConfig
import org.sagebionetworks.research.mobiletoolbox.app.R
import org.sagebionetworks.research.mobiletoolbox.app.databinding.FragmentProfileBinding
import org.sagebionetworks.research.mobiletoolbox.app.databinding.PropertyRowBinding
import org.sagebionetworks.research.mobiletoolbox.app.ui.study.StudyFragment


class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private val viewModel: AccountViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater)
        viewModel.authRepo.currentStudyId()?.let{
            val propBinding = PropertyRowBinding.inflate(inflater)
            propBinding.name.text = getString(R.string.study_id_label)
            propBinding.value.text = it
            binding.profileContainer.addView(propBinding.root)
        }
        viewModel.authRepo.session()?.let { userSessionInfo ->
            userSessionInfo.externalId?.let {
                val participantId = it.substringBefore(":")
                val propBinding = PropertyRowBinding.inflate(inflater)
                propBinding.name.text = getString(R.string.participant_id_label)
                propBinding.value.text = participantId
                binding.profileContainer.addView(propBinding.root)
            }
            userSessionInfo.phone?.let { phone ->
                val propBinding = PropertyRowBinding.inflate(inflater)
                propBinding.name.text = getString(R.string.phone_label  )
                propBinding.value.text = phone.nationalFormat ?: phone.number
                binding.profileContainer.addView(propBinding.root)
            }
        }
        val propBinding = PropertyRowBinding.inflate(inflater)
        propBinding.name.text = getString(R.string.version_label)
        propBinding.value.text = BuildConfig.VERSION_NAME
        binding.profileContainer.addView(propBinding.root)

        binding.withdrawInfo.setOnClickListener {
            val navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_mtb_main)
            val bundle = Bundle()
            bundle.putInt(StudyFragment.KEY_PAGE_INDEX, StudyFragment.CONTACT_PAGE_INDEX)
            navController.navigate(R.id.navigation_study_info, bundle)
        }

        binding.logoutButton.setOnClickListener {
            val newFragment = ConfirmLogOutDialogFragment(viewModel.authRepo)
            newFragment.show(parentFragmentManager, "missiles")
        }

        return binding.root
    }


}

class ConfirmLogOutDialogFragment(val authRepo: AuthenticationRepository) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)
            builder.setTitle(R.string.confirm_log_out_title)
                .setMessage(R.string.confirm_log_out_message)
                .setPositiveButton(R.string.yes_log_out,
                    DialogInterface.OnClickListener { dialog, id ->
                        MainScope().launch {
                            authRepo.signOut()
                            //TODO: Need better app wide handling of user no longer being authenticated.
                            // For now finishing the activity will close the app - nbrown 8/25/2021
                            requireActivity().finish()
                        }
                    })
                .setNegativeButton(R.string.cancel,
                    DialogInterface.OnClickListener { dialog, id ->
                        // User cancelled the dialog
                    })
            // Create the AlertDialog object and return it
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}