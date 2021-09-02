package org.sagebionetworks.research.mtb.alpha_app.ui.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.sagebionetworks.research.mtb.alpha_app.R
import org.sagebionetworks.research.mtb.alpha_app.databinding.FragmentAccountBinding

class AccountFragment : Fragment() {

    private val accountViewModel: AccountViewModel by viewModel()
    private lateinit var binding: FragmentAccountBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentAccountBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.viewPager.adapter = AccountPagerAdapter(this)

        // Set the icon and text for each tab
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.setIcon(getTabIcon(position))
            tab.text = getTabTitle(position)
        }.attach()

        return root
    }

    private fun getTabIcon(position: Int): Int {
        return when (position) {
            PROFILE_PAGE_INDEX -> R.drawable.ic_nav_account
            SETTINGS_PAGE_INDEX -> R.drawable.ic_nav_settings
            else -> throw IndexOutOfBoundsException()
        }
    }

    private fun getTabTitle(position: Int): String? {
        return when (position) {
            PROFILE_PAGE_INDEX -> getString(R.string.profile)
            SETTINGS_PAGE_INDEX -> getString(R.string.settings)
            else -> null
        }
    }

}

const val PROFILE_PAGE_INDEX = 0
const val SETTINGS_PAGE_INDEX = 1

class AccountPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    /**
     * Mapping of the ViewPager page indexes to their respective Fragments
     */
    private val tabFragmentsCreators: Map<Int, () -> Fragment> = mapOf(
        PROFILE_PAGE_INDEX to { ProfileFragment() },
        SETTINGS_PAGE_INDEX to { SettingsFragment() }
    )

    override fun getItemCount() = tabFragmentsCreators.size

    override fun createFragment(position: Int): Fragment {
        return tabFragmentsCreators[position]?.invoke() ?: throw IndexOutOfBoundsException()
    }
}