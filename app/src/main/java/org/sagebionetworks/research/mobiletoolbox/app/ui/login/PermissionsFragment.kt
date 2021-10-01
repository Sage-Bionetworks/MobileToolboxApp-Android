package org.sagebionetworks.research.mobiletoolbox.app.ui.login

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import org.sagebionetworks.research.mobiletoolbox.app.R
import org.sagebionetworks.research.mobiletoolbox.app.databinding.FragmentPermissionPageBinding
import org.sagebionetworks.research.mobiletoolbox.app.databinding.FragmentPermissionsBinding

class PermissionsFragment : Fragment() {

    private lateinit var binding: FragmentPermissionsBinding
    private lateinit var pageTypeMap: Map<Int, PermissionPageType>

    val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            // Don't care what user answered, just go to next screen.
            goNext()
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentPermissionsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        //TODO: Determine which pages to show based on appConfig -nbrown 9/28/2021
        pageTypeMap = mapOf(
            0 to PermissionPageType.NOTIFICATION_PAGE,
            1 to PermissionPageType.INTRO_PAGE,
            2 to PermissionPageType.LOCATION_PAGE,
            3 to PermissionPageType.MICROPHONE_PAGE,
            4 to PermissionPageType.MOTION_PAGE
        )
        binding.viewPager.adapter = PermissionsPagerAdapter(pageTypeMap,this)
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

    private fun onNextClicked() {
        val curIndex = binding.viewPager.currentItem
        val permission = pageTypeMap[curIndex]?.permission
        if (permission != null) {
            requestPermission(permission)
        } else {
            goNext()
        }
    }

    private fun goNext() {
        val curIndex = binding.viewPager.currentItem
        if (curIndex < binding.viewPager.adapter!!.itemCount - 1) {
            binding.viewPager.setCurrentItem(curIndex + 1, true)
            return
        }
        requireActivity().setResult(AppCompatActivity.RESULT_OK)
        requireActivity().finish()
    }

    private fun onPrevClicked() {
        val curIndex = binding.viewPager.currentItem
        if (curIndex > 0) {
            binding.viewPager.setCurrentItem(curIndex - 1, true)
            return
        }
        parentFragmentManager.popBackStack()
    }

    private fun requestPermission(permission: String) {
        if (ContextCompat.checkSelfPermission(requireContext(), permission) ==
            PackageManager.PERMISSION_GRANTED) {
            goNext()
        } else {
            requestPermissionLauncher.launch(permission)
        }
    }

    companion object {

        @JvmStatic
        fun newInstance() = PermissionsFragment()
    }
}

enum class PermissionPageType(
    val permission: String?,
    val iconResource: Int,
    val headerStringIdentifier: Int,
    val bodyStringIdentifier: Int
) {
    NOTIFICATION_PAGE(null, R.drawable.ic_perm_notifications, R.string.notifications_header, R.string.notifications_body),
    INTRO_PAGE(null, R.drawable.ic_perm_environmental_factors, R.string.intro_header, R.string.intro_body),
    LOCATION_PAGE(Manifest.permission.ACCESS_COARSE_LOCATION, R.drawable.ic_perm_weather_air, R.string.location_header, R.string.location_body),
    MICROPHONE_PAGE(Manifest.permission.RECORD_AUDIO, R.drawable.ic_perm_microphone, R.string.microphone_header, R.string.microphone_body),
    MOTION_PAGE(null, R.drawable.ic_perm_motion_fitness, R.string.motion_sensor_header, R.string.motion_sensor_body)
}

class PermissionsPagerAdapter(private val pageTypeMap: Map<Int, PermissionPageType>, fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount() = pageTypeMap.count()

    override fun createFragment(position: Int): Fragment {
        val permissionPage = pageTypeMap[position] ?:throw IndexOutOfBoundsException()
        return PermissionPageFragment.newInstance(permissionPage)
    }
}

class PermissionPageFragment : Fragment() {

    companion object {

        const val KEY_PERMISSION_PAGE = "key_permission_page"

        fun newInstance(permissionPageType: PermissionPageType): PermissionPageFragment {
            return PermissionPageFragment().apply {
                arguments = Bundle(1).apply {
                    putString(KEY_PERMISSION_PAGE, permissionPageType.name)
                }
            }
        }
    }

    private lateinit var binding: FragmentPermissionPageBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPermissionPageBinding.inflate(inflater, container, false)
        val permissionPageString = arguments?.getString(KEY_PERMISSION_PAGE) ?: throw IllegalArgumentException()
        val permissionPage = PermissionPageType.valueOf(permissionPageString)
        binding.details.logo.setImageResource(permissionPage.iconResource)
        binding.details.header.text = getString(permissionPage.headerStringIdentifier)
        binding.details.body.text = getString(permissionPage.bodyStringIdentifier)
        return binding.root
    }


}
