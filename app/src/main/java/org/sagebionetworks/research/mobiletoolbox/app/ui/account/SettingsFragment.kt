package org.sagebionetworks.research.mobiletoolbox.app.ui.account

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import org.sagebionetworks.research.mobiletoolbox.app.R
import org.sagebionetworks.research.mobiletoolbox.app.databinding.FragmentSettingsBinding
import org.sagebionetworks.research.mobiletoolbox.app.databinding.PermissionDetailsBinding
import org.sagebionetworks.research.mobiletoolbox.app.ui.login.PermissionPageType


class SettingsFragment : Fragment() {

    companion object {

        const val KEY_PERMISSION_PAGE_TYPES = "key_permission_page_types"

        fun newInstance(permissionPageTypes: List<PermissionPageType>): SettingsFragment {
            return SettingsFragment().apply {
                arguments = Bundle(1).apply {
                    val stringArray = permissionPageTypes.map { permissionPageType -> permissionPageType.name }.toTypedArray()
                    putStringArray(KEY_PERMISSION_PAGE_TYPES, stringArray)
                }
            }
        }
    }

    private lateinit var binding: FragmentSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val permissionPages = arguments?.getStringArray(KEY_PERMISSION_PAGE_TYPES) ?: throw IllegalArgumentException()
        val permissionPageTypes = permissionPages.map { PermissionPageType.valueOf(it) }
        for (permissionPage in permissionPageTypes) {
            val permissionBinding = PermissionDetailsBinding.inflate(inflater, binding.content, false)
            permissionBinding.logo.setImageResource(permissionPage.iconResource)
            permissionBinding.header.text = getString(permissionPage.headerStringIdentifier)
            permissionBinding.body.text = getString(permissionPage.bodyStringIdentifier)
            configureChangeButton(permissionBinding.changeButton, permissionPage)
            binding.content.addView(permissionBinding.root)

        }
        if (permissionPageTypes.size == 1 && permissionPageTypes.get(0) == PermissionPageType.NOTIFICATION_PAGE) {
            binding.title.visibility = View.GONE
        }
        return binding.root
    }

    private fun configureChangeButton(button: Button, permissionPage: PermissionPageType) {
        button.setOnClickListener {
            if (permissionPage == PermissionPageType.NOTIFICATION_PAGE && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                //On new versions of Android we can take user straight to notification settings
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, requireContext().packageName);
                startActivity(intent);
            } else {
                //Take user to application settings
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.parse("package:" + requireContext().packageName)
                startActivity(intent)
            }
        }
        when(permissionPage) {
            PermissionPageType.NOTIFICATION_PAGE -> {
                button.visibility = View.VISIBLE
                button.setBackgroundColor(resources.getColor(R.color.backgroundPurple))
            }
            PermissionPageType.LOCATION_PAGE -> {
                button.visibility = View.VISIBLE
                button.setBackgroundColor(resources.getColor(R.color.lightGreen))
            }
            PermissionPageType.MICROPHONE_PAGE -> {
                button.visibility = View.VISIBLE
                button.setBackgroundColor(resources.getColor(R.color.backgroundBlue))
            }

        }
    }

}