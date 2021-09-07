package org.sagebionetworks.research.mobiletoolbox.app.ui.study

import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.sagebionetworks.bridge.kmm.shared.cache.ResourceResult
import org.sagebionetworks.bridge.kmm.shared.models.Contact
import org.sagebionetworks.bridge.kmm.shared.models.ContactRole
import org.sagebionetworks.bridge.kmm.shared.models.Study
import org.sagebionetworks.research.mobiletoolbox.app.R
import org.sagebionetworks.research.mobiletoolbox.app.databinding.ContactBinding
import org.sagebionetworks.research.mobiletoolbox.app.databinding.FragmentAboutStudyBinding

class AboutStudyFragment : Fragment() {

    private val viewModel: StudyInfoViewModel by viewModel()
    lateinit var binding: FragmentAboutStudyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.loadStudy()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel.studyLiveData.observe(viewLifecycleOwner, Observer {
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
        binding = FragmentAboutStudyBinding.inflate(inflater)
        binding.privacyNoticeButton.setOnClickListener {
            val launchIntent = Intent(requireContext(), PrivacyNoticeActivity::class.java)
            startActivity(launchIntent)
        }
        return binding.root
    }

    private fun studyLoaded(study: Study) {
        binding.logoBackground.setBackgroundColor(Color.parseColor(study.colorScheme?.background ?: "#FFFFFF"))
        study.studyLogoUrl?.let {
            Glide.with(this).load(it).into(binding.logo);
        }

        binding.title.text = study.name
        binding.details.text = study.details

        val studyRoles = listOf(ContactRole.PRINCIPAL_INVESTIGATOR, ContactRole.INVESTIGATOR, ContactRole.SPONSOR)
        val contacts = study.contacts
            ?.filter { contact ->  studyRoles.contains(contact.role) }
            ?.sortedBy { contact: Contact ->  studyRoles.indexOf(contact.role) } ?: listOf()

        val firstAffiliatedContact = contacts.firstOrNull{it.affiliation != null}
        val institutionName = firstAffiliatedContact?.affiliation

        val studyContacts = contacts.map { StudyContact(it, resources) }.toMutableList()
        institutionName?.let {
            studyContacts.add(1, StudyContact(institutionName, getString(R.string.institution)))
        }

        for(contact in studyContacts) {
            binding.contacts.addView(createContactView(contact))
        }

    }

    private fun createContactView(contact: StudyContact): View {
        val contactBinding = ContactBinding.inflate(layoutInflater)
        contactBinding.name.text = contact.name
        contactBinding.position.text = contact.position
        contactBinding.phone.visibility = View.GONE
        contactBinding.phoneIcon.visibility = View.GONE
        contactBinding.email.visibility = View.GONE
        contactBinding.emailIcon.visibility = View.GONE
        return contactBinding.root
    }

    data class StudyContact(val name: String, val position: String) {

        constructor(contact: Contact, resources: Resources):
            this(contact.name, contact.position ?: when(contact.role) {
                ContactRole.INVESTIGATOR -> resources.getString(R.string.investigator)
                ContactRole.IRB -> resources.getString(R.string.irb_ethics_board)
                ContactRole.PRINCIPAL_INVESTIGATOR -> resources.getString(R.string.principal_investigator)
                ContactRole.SPONSOR -> resources.getString(R.string.funder)
                else -> resources.getString(R.string.study_support)
        } )
    }

}