package org.sagebionetworks.research.mobiletoolbox.app.ui.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.sagebionetworks.bridge.kmm.shared.cache.ResourceResult
import org.sagebionetworks.bridge.kmm.shared.models.Study
import org.sagebionetworks.research.mobiletoolbox.app.MtbBaseFragment
import org.sagebionetworks.research.mobiletoolbox.app.R
import org.sagebionetworks.research.mobiletoolbox.app.databinding.FragmentAccountBinding
import org.sagebionetworks.research.mobiletoolbox.app.recorder.backgroundRecorders
import org.sagebionetworks.research.mobiletoolbox.app.ui.login.PermissionPageType
import org.sagebionetworks.research.mobiletoolbox.app.ui.study.StudyViewModel

class AccountFragment : MtbBaseFragment() {

    private val accountViewModel: AccountViewModel by viewModel()
    private val studyViewModel: StudyViewModel by viewModel()
    private lateinit var binding: FragmentAccountBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentAccountBinding.inflate(inflater, container, false)
        //Show loading until we have study loaded and know what to show.
        binding.progressOverlay.progressOverlay.visibility = View.VISIBLE
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        studyViewModel.studyLiveData.observe(viewLifecycleOwner, {
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
        studyViewModel.loadStudy()
    }

    private fun studyLoaded(study: Study) {
        binding.progressOverlay.progressOverlay.visibility = View.GONE
        binding.viewPager.adapter = AccountPagerAdapter(this, study)
        binding.tabLayout.tabIconTint = null

        // Set the icon and text for each tab
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.setIcon(getTabIcon(position))
            tab.text = getTabTitle(position)
        }.attach()
    }

    private fun getTabIcon(position: Int): Int {
        return when (position) {
            PROFILE_PAGE_INDEX -> R.drawable.nav_account
            NOTIFICATION_PAGE_INDEX -> R.drawable.nav_notifications
            SETTINGS_PAGE_INDEX -> R.drawable.nav_settings
            else -> throw IndexOutOfBoundsException()
        }
    }

    private fun getTabTitle(position: Int): String? {
        return when (position) {
            PROFILE_PAGE_INDEX -> getString(R.string.profile)
            NOTIFICATION_PAGE_INDEX -> getString(R.string.notifications)
            SETTINGS_PAGE_INDEX -> getString(R.string.settings)
            else -> null
        }
    }

}

const val PROFILE_PAGE_INDEX = 0
const val NOTIFICATION_PAGE_INDEX = 1
const val SETTINGS_PAGE_INDEX = 2

class AccountPagerAdapter(fragment: Fragment, val study: Study) : FragmentStateAdapter(fragment) {

    /**
     * Mapping of the ViewPager page indexes to their respective Fragments
     */
    private val tabFragmentsCreators: Map<Int, () -> Fragment> = mapOf(
        PROFILE_PAGE_INDEX to { ProfileFragment() },
        NOTIFICATION_PAGE_INDEX to { SettingsFragment.newInstance(listOf(PermissionPageType.NOTIFICATION_PAGE)) },
        SETTINGS_PAGE_INDEX to { SettingsFragment.newInstance(settingsPageList) }
    )

    private val settingsPageList : List<PermissionPageType> =
        mutableListOf<PermissionPageType>().apply {
            study.backgroundRecorders?.let { recorderMap ->
                val showWeather = recorderMap.getOrDefault("weather", false)
                val showMicrophone = recorderMap.getOrDefault("microphone", false)
                val showMotion = recorderMap.getOrDefault("motion", false)
                if (showWeather) {
                    this.add(PermissionPageType.LOCATION_PAGE)
                }
                if (showMicrophone) {
                    this.add(PermissionPageType.MICROPHONE_PAGE)
                }
                if (showMotion) {
                    this.add(PermissionPageType.MOTION_PAGE)
                }
            }
        }


    override fun getItemCount() = if (settingsPageList.isNotEmpty()) {3} else {2}

    override fun createFragment(position: Int): Fragment {
        return tabFragmentsCreators[position]?.invoke() ?: throw IndexOutOfBoundsException()
    }
}