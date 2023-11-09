package org.sagebionetworks.research.mobiletoolbox.app.ui.study

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import org.sagebionetworks.research.mobiletoolbox.app.R
import org.sagebionetworks.research.mobiletoolbox.app.databinding.FragmentPrivacyNoticeBinding
import org.sagebionetworks.research.mobiletoolbox.app.databinding.FragmentPrivacyPageBinding
import org.sagebionetworks.research.mobiletoolbox.app.databinding.PrivacyNoticeRowBinding


class PrivacyNoticeFragment : Fragment() {

    private lateinit var binding: FragmentPrivacyNoticeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentPrivacyNoticeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.viewPager.adapter = PrivacyPagerAdapter(this)
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = getTabTitle(position)
        }.attach()

        return root
    }

    private fun getTabTitle(position: Int): String? {
        return when (position) {
            WE_WILL_PAGE_INDEX -> getString(R.string.we_will)
            WE_WONT_PAGE_INDEX -> getString(R.string.we_wont)
            YOU_CAN_PAGE_INDEX -> getString(R.string.you_can)
            else -> null
        }
    }

    fun goNext(): Boolean {
        val curIndex = binding.viewPager.currentItem
        if (curIndex < 2) {
            binding.viewPager.setCurrentItem(curIndex + 1, true)
            return true
        }
        return false
    }

    fun goPrev(): Boolean {
        val curIndex = binding.viewPager.currentItem
        if (curIndex > 0) {
            binding.viewPager.setCurrentItem(curIndex - 1, true)
            return true
        }
        return false
    }

}

const val WE_WILL_PAGE_INDEX = 0
const val WE_WONT_PAGE_INDEX = 1
const val YOU_CAN_PAGE_INDEX = 2

class PrivacyPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    /**
     * Mapping of the ViewPager page indexes to their respective Fragments
     */
    private val tabFragmentsCreators: Map<Int, () -> Fragment> = mapOf(
        WE_WILL_PAGE_INDEX to { PrivacyPageFragment.newInstance(WE_WILL_PAGE_INDEX) },
        WE_WONT_PAGE_INDEX to { PrivacyPageFragment.newInstance(WE_WONT_PAGE_INDEX) },
        YOU_CAN_PAGE_INDEX to { PrivacyPageFragment.newInstance(YOU_CAN_PAGE_INDEX) }
    )

    override fun getItemCount() = tabFragmentsCreators.size

    override fun createFragment(position: Int): Fragment {
        return tabFragmentsCreators[position]?.invoke() ?: throw IndexOutOfBoundsException()
    }
}

class PrivacyPageFragment : Fragment() {

    companion object {

        const val KEY_PAGE_INDEX = "key_page_index"

        fun newInstance(pageIndex: Int): PrivacyPageFragment {
            return PrivacyPageFragment().apply {
                arguments = Bundle(1).apply {
                    putInt(KEY_PAGE_INDEX, pageIndex)
                }
            }
        }
    }

    private lateinit var binding: FragmentPrivacyPageBinding

    private val privacyNoticeListMap: Map<Int, List<PrivacyNotice>> = mapOf(
        WE_WILL_PAGE_INDEX to listOf(
            PrivacyNotice(R.drawable.ic_privacy_eyes, R.string.privacy_we_will_collect_pii),
            PrivacyNotice(R.drawable.ic_privacy_graph, R.string.privacy_we_will_collect_data),
            PrivacyNotice(R.drawable.ic_privacy_privacybits, R.string.privacy_we_will_protect_data),
            PrivacyNotice(R.drawable.ic_privacy_globe, R.string.privacy_we_will_share_data),
            PrivacyNotice(R.drawable.ic_privacy_server, R.string.privacy_we_will_store_data),
            PrivacyNotice(R.drawable.ic_privacy_trash, R.string.privacy_we_will_delete_data),
            PrivacyNotice(R.drawable.ic_privacy_notify, R.string.privacy_we_will_notify_changes),
        ),
        WE_WONT_PAGE_INDEX to listOf(
            PrivacyNotice(R.drawable.ic_privacy_contact, R.string.privacy_we_wont_access_contact),
            PrivacyNotice(R.drawable.ic_privacy_profilecard, R.string.privacy_we_wont_sell_data),
            PrivacyNotice(R.drawable.ic_privacy_tracking, R.string.privacy_we_wont_track_other_apps),
            PrivacyNotice(R.drawable.ic_privacy_advertising, R.string.privacy_we_wont_use_for_advertising),
        ),
        YOU_CAN_PAGE_INDEX to listOf(
            PrivacyNotice(R.drawable.ic_privacy_permission, R.string.privacy_you_can_request_access),
            PrivacyNotice(R.drawable.ic_privacy_passivedata, R.string.privacy_you_can_allow_passive_data),
            PrivacyNotice(R.drawable.ic_privacy_backgroundinfo, R.string.privacy_you_can_optional_permissions),
            PrivacyNotice(R.drawable.ic_privacy_micandvid, R.string.privacy_you_can_permit_microphone),
            PrivacyNotice(R.drawable.ic_privacy_sharedata, R.string.privacy_you_can_choose_share),
            PrivacyNotice(R.drawable.ic_privacy_optin, R.string.privacy_you_can_recieve_notifications),
        )
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPrivacyPageBinding.inflate(inflater, container, false)
        binding.content.removeAllViews()
        val pageIndex = arguments?.getInt(KEY_PAGE_INDEX) ?: throw IllegalArgumentException()
        val noticeList = privacyNoticeListMap[pageIndex]
        noticeList?.forEach {
            val rowBinding = PrivacyNoticeRowBinding.inflate(inflater, binding.content, false)
            rowBinding.icon.setImageResource(it.iconIdentifier)
            rowBinding.text.text = getString(it.stringIdentifier)
            binding.content.addView(rowBinding.root)
        }
        binding.fullNoticeButton.setOnClickListener {
            val uriFile: Uri = Uri.parse("https://studies.bridgedigital.health/privacy_policy.pdf")

            val intent = Intent(Intent.ACTION_VIEW).apply {
                type = "application/pdf"
                data = uriFile
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.privacy_policy))
            }
            startActivity(Intent.createChooser(intent, getString(R.string.privacy_policy)))
        }
        return binding.root
    }

    data class PrivacyNotice(
        val iconIdentifier: Int,
        val stringIdentifier: Int
    )

}

