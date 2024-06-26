package org.sagebionetworks.research.mobiletoolbox.app.ui.study

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import org.sagebionetworks.research.mobiletoolbox.app.MtbBaseFragment
import org.sagebionetworks.research.mobiletoolbox.app.R
import org.sagebionetworks.research.mobiletoolbox.app.databinding.FragmentStudyInfoBinding

class StudyFragment : MtbBaseFragment() {

    companion object {

        const val KEY_PAGE_INDEX = "key_page_index"
        const val ABOUT_STUDY_PAGE_INDEX = 0
        const val CONTACT_PAGE_INDEX = 1
    }

    private var _binding: FragmentStudyInfoBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentStudyInfoBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.viewPager.adapter = StudyPagerAdapter(this)

        binding.tabLayout.tabIconTint = null
        // Set the icon and text for each tab
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.setIcon(getTabIcon(position))
            tab.text = getTabTitle(position)
        }.attach()

        return root
    }

    override fun onResume() {
        super.onResume()
        arguments?.getInt(KEY_PAGE_INDEX, -1)?.let {
            if (it >= 0) {
                binding.viewPager.setCurrentItem(it, false)
            }
        }
    }

    private fun getTabIcon(position: Int): Int {
        return when (position) {
            ABOUT_STUDY_PAGE_INDEX -> R.drawable.about_study
            CONTACT_PAGE_INDEX -> R.drawable.contact_support
            else -> throw IndexOutOfBoundsException()
        }
    }

    private fun getTabTitle(position: Int): String? {
        return when (position) {
            ABOUT_STUDY_PAGE_INDEX -> getString(R.string.about_the_study)
            CONTACT_PAGE_INDEX -> getString(R.string.contact_and_support)
            else -> null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}


class StudyPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    /**
     * Mapping of the ViewPager page indexes to their respective Fragments
     */
    private val tabFragmentsCreators: Map<Int, () -> Fragment> = mapOf(
        StudyFragment.ABOUT_STUDY_PAGE_INDEX to { AboutStudyFragment() },
        StudyFragment.CONTACT_PAGE_INDEX to { SupportFragment() }
    )

    override fun getItemCount() = tabFragmentsCreators.size

    override fun createFragment(position: Int): Fragment {
        return tabFragmentsCreators[position]?.invoke() ?: throw IndexOutOfBoundsException()
    }
}