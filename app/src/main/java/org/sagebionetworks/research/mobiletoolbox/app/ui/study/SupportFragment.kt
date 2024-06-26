package org.sagebionetworks.research.mobiletoolbox.app.ui.study

import android.os.Bundle
import android.text.Html
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.sagebionetworks.bridge.kmm.shared.cache.ResourceResult
import org.sagebionetworks.bridge.kmm.shared.models.Contact
import org.sagebionetworks.bridge.kmm.shared.models.ContactRole
import org.sagebionetworks.bridge.kmm.shared.models.Study
import org.sagebionetworks.research.mobiletoolbox.app.R
import org.sagebionetworks.research.mobiletoolbox.app.databinding.ContactBinding
import org.sagebionetworks.research.mobiletoolbox.app.databinding.FragmentSupportBinding

class SupportFragment : Fragment() {

    private val viewModel: StudyViewModel by viewModel()
    private lateinit var binding: FragmentSupportBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.loadStudy()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel.studyLiveData.observe(viewLifecycleOwner, {
            when(it) {
                is ResourceResult.Success -> {
                    studyLoaded(it.data)
                }
                is ResourceResult.InProgress -> {

                }
                is ResourceResult.Failed -> {

                }
            }
        })
        // Inflate the layout for this fragment
        binding = FragmentSupportBinding.inflate(inflater)
        return binding.root
    }

    private fun studyLoaded(study: Study) {
        binding.studyId.text = Html.fromHtml(getString(R.string.study_id_bold, study.identifier))
        viewModel.userSessionInfo?.externalId?.let {
            val participantId = it.substringBefore(":")
            binding.participantId.text = Html.fromHtml(getString(R.string.participant_id, participantId))
            binding.participantId.visibility = View.VISIBLE
        }
        viewModel.userSessionInfo?.phone?.let {
            binding.registrationPhone.text = Html.fromHtml(getString(R.string.registration_phone_number, it.number))
            binding.registrationPhone.visibility = View.VISIBLE
        }

        binding.supportContacts.removeAllViews()
        val supportContacts = study.contacts?.filter { it.role == ContactRole.STUDY_SUPPORT } ?: listOf()
        for(contact in supportContacts) {
            binding.supportContacts.addView(createContactView(contact))
        }
        binding.irbContacts.removeAllViews()
        val irbContacts = study.contacts?.filter { it.role == ContactRole.IRB } ?: listOf()
        for(contact in irbContacts) {
            binding.irbContacts.addView(createContactView(contact))
        }
    }

    private fun createContactView(contact: Contact): View {
        val contactBinding = ContactBinding.inflate(layoutInflater)
        contactBinding.name.text = contact.name
        contactBinding.position.text = contact.position
        contactBinding.email.text = contact.email
        contactBinding.phone.text = contact.phone?.nationalFormat ?: contact.phone?.number
        return contactBinding.root
    }
}