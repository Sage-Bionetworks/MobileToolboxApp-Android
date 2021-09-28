package org.sagebionetworks.research.mobiletoolbox.app.ui.login

import android.os.Bundle
import android.text.Html
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import org.sagebionetworks.research.mobiletoolbox.app.R
import org.sagebionetworks.research.mobiletoolbox.app.databinding.FragmentPermissionPageBinding
import org.sagebionetworks.research.mobiletoolbox.app.databinding.FragmentPermissionsBinding
import org.sagebionetworks.research.mobiletoolbox.app.databinding.FragmentPrivacyNoticeBinding
import org.sagebionetworks.research.mobiletoolbox.app.databinding.FragmentPrivacyPageBinding
import org.sagebionetworks.research.mobiletoolbox.app.databinding.PrivacyNoticeRowBinding
import org.sagebionetworks.research.mobiletoolbox.app.ui.study.PrivacyNoticeFragment
import org.sagebionetworks.research.mobiletoolbox.app.ui.study.PrivacyPageFragment
import org.sagebionetworks.research.mobiletoolbox.app.ui.study.PrivacyPagerAdapter
import org.sagebionetworks.research.mobiletoolbox.app.ui.study.WE_WILL_PAGE_INDEX
import org.sagebionetworks.research.mobiletoolbox.app.ui.study.WE_WONT_PAGE_INDEX
import org.sagebionetworks.research.mobiletoolbox.app.ui.study.YOU_CAN_PAGE_INDEX


/**
 * A simple [Fragment] subclass.
 * Use the [PermissionsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PermissionsFragment : Fragment() {

    private lateinit var binding: FragmentPermissionsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentPermissionsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        //TODO: Determine which pages to show based on appConfig -nbrown 9/28/2021
        val pageMap = mapOf(
            0 to PermissionPage.NOTIFICATION_PAGE,
            1 to PermissionPage.INTRO_PAGE,
            2 to PermissionPage.LOCATION_PAGE,
            3 to PermissionPage.MICROPHONE_PAGE,
            4 to PermissionPage.MOTION_PAGE
        )
        binding.viewPager.adapter = PermissionsPagerAdapter(pageMap,this)
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->

        }.attach()

        binding.nextButton.setOnClickListener {
            onNextClicked()
        }
        binding.prevButton.setOnClickListener {
            onPrevClicked()
        }

        return root
    }

    fun onNextClicked() {
        val curIndex = binding.viewPager.currentItem
        if (curIndex < binding.viewPager.adapter!!.itemCount - 1) {
            binding.viewPager.setCurrentItem(curIndex + 1, true)
            return
        }
        requireActivity().setResult(AppCompatActivity.RESULT_OK)
        requireActivity().finish()
    }

    fun onPrevClicked() {
        val curIndex = binding.viewPager.currentItem
        if (curIndex > 0) {
            binding.viewPager.setCurrentItem(curIndex - 1, true)
            return
        }
        parentFragmentManager.popBackStack()
    }

    companion object {

        @JvmStatic
        fun newInstance() = PermissionsFragment()
    }
}

enum class PermissionPage {
    NOTIFICATION_PAGE,
    INTRO_PAGE,
    LOCATION_PAGE,
    MICROPHONE_PAGE,
    MOTION_PAGE
}

class PermissionsPagerAdapter(private val pageMap: Map<Int, PermissionPage>, fragment: Fragment) : FragmentStateAdapter(fragment) {

    /**
     * Mapping of the ViewPager page indexes to their respective Fragments
     */
    private val tabFragmentsCreators: Map<PermissionPage, () -> Fragment> = mapOf(
        PermissionPage.NOTIFICATION_PAGE to { PermissionPageFragment.newInstance(R.drawable.ic_perm_notifications, R.string.notifications_header, R.string.notifications_body) },
        PermissionPage.INTRO_PAGE to { PermissionPageFragment.newInstance(R.drawable.ic_perm_environmental_factors, R.string.intro_header, R.string.intro_body) },
        PermissionPage.LOCATION_PAGE to { PermissionPageFragment.newInstance(R.drawable.ic_perm_weather_air, R.string.location_header, R.string.location_body) },
        PermissionPage.MICROPHONE_PAGE to { PermissionPageFragment.newInstance(R.drawable.ic_perm_microphone, R.string.microphone_header, R.string.microphone_body) },
        PermissionPage.MOTION_PAGE to { PermissionPageFragment.newInstance(R.drawable.ic_perm_motion_fitness, R.string.motion_sensor_header, R.string.motion_sensor_body) },

    )

    override fun getItemCount() = pageMap.count()

    override fun createFragment(position: Int): Fragment {
        return tabFragmentsCreators[pageMap[position]]?.invoke() ?: throw IndexOutOfBoundsException()
    }
}

class PermissionPageFragment : Fragment() {

    companion object {

        const val KEY_ICON_RESOURCE = "key_icon_resource"
        const val KEY_HEADER_STRING_RESOURCE = "key_header_string_resource"
        const val KEY_BODY_STRING_RESOURCE = "key_body_string_resource"

        fun newInstance(iconIdentifier: Int, headerStringIdentifier: Int, bodyStringIdentifier: Int): PermissionPageFragment {
            return PermissionPageFragment().apply {
                arguments = Bundle(1).apply {
                    putInt(KEY_ICON_RESOURCE, iconIdentifier)
                    putInt(KEY_HEADER_STRING_RESOURCE, headerStringIdentifier)
                    putInt(KEY_BODY_STRING_RESOURCE, bodyStringIdentifier)
                }
            }
        }
    }

    private lateinit var binding: FragmentPermissionPageBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPermissionPageBinding.inflate(inflater, container, false)
        val iconIdentifier = arguments?.getInt(KEY_ICON_RESOURCE) ?: throw IllegalArgumentException()
        val headerStringIdentifier = arguments?.getInt(KEY_HEADER_STRING_RESOURCE) ?: throw IllegalArgumentException()
        val bodyStringIdentifier = arguments?.getInt(KEY_BODY_STRING_RESOURCE) ?: throw IllegalArgumentException()
        binding.logo.setImageResource(iconIdentifier)
        binding.header.text = getString(headerStringIdentifier)
        binding.body.text = getString(bodyStringIdentifier)
        return binding.root
    }

}