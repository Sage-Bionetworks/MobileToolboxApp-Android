package org.sagebionetworks.research.mobiletoolbox.app.ui.login

import android.Manifest
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.sagebionetworks.research.mobiletoolbox.app.R
import org.sagebionetworks.research.mobiletoolbox.app.databinding.FragmentPermissionPageBinding
import org.sagebionetworks.research.mobiletoolbox.app.databinding.FragmentPermissionsBinding
import org.sagebionetworks.research.mobiletoolbox.app.databinding.PermissionDetailsBinding
import org.sagebionetworks.research.mobiletoolbox.app.recorder.backgroundRecorders

class PermissionsFragment : Fragment() {

    lateinit var binding: FragmentPermissionsBinding
    private lateinit var pageTypeMap: Map<Int, PermissionPageType>
    private val viewModel: LoginViewModel by sharedViewModel()

    private val requestPermissionLauncher =
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


        val mutablePageMap = mutableMapOf<Int, PermissionPageType>()
        mutablePageMap[0] = PermissionPageType.NOTIFICATION_PAGE
        viewModel.study?.backgroundRecorders?.let { recorderMap ->
            val showWeather = recorderMap.getOrDefault("weather", false)
            val showMicrophone = recorderMap.getOrDefault("microphone", false)
            val showMotion = recorderMap.getOrDefault("motion", false)
            var index = 1
            if (showWeather || showMicrophone || showMotion) {
                mutablePageMap[index] = PermissionPageType.INTRO_PAGE
                index++
            }
            if (showWeather) {
                mutablePageMap[index] = PermissionPageType.LOCATION_PAGE
                index++
            }
            if (showMicrophone) {
                mutablePageMap[index] = PermissionPageType.MICROPHONE_PAGE
                index++
            }
            if (showMotion) {
                mutablePageMap[index] = PermissionPageType.MOTION_PAGE
            }
        }
        pageTypeMap = mutablePageMap

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
    val bodyStringIdentifier: Int,
    val showToggle: Boolean = false,
) {
    NOTIFICATION_PAGE(null, R.drawable.ic_perm_notifications, R.string.notifications_header, R.string.notifications_body),
    INTRO_PAGE(null, R.drawable.ic_perm_environmental_factors, R.string.intro_header, R.string.intro_body),
    LOCATION_PAGE(Manifest.permission.ACCESS_COARSE_LOCATION, R.drawable.ic_perm_weather_air, R.string.location_header, R.string.location_body),
    MICROPHONE_PAGE(Manifest.permission.RECORD_AUDIO, R.drawable.ic_perm_microphone, R.string.microphone_header, R.string.microphone_body),
    MOTION_PAGE(null, R.drawable.ic_perm_motion_fitness,
        R.string.motion_sensor_header,
        R.string.motion_sensor_body,
        showToggle = true,
    ) {
        override fun updateAllowToggle(context: Context, allow: Boolean) {
            val sharedPref = context.getSharedPreferences(PERMISSION_PREFERENCES_FILE, MODE_PRIVATE)
            sharedPref.edit().putBoolean(ALLOW_MOTION_KEY, allow).apply()
        }

        override fun getAllowToggle(context: Context): Boolean? {
            val sharedPref = context.getSharedPreferences(PERMISSION_PREFERENCES_FILE, MODE_PRIVATE)
            return if (sharedPref.contains(ALLOW_MOTION_KEY)) {
                sharedPref.getBoolean(ALLOW_MOTION_KEY, false)
            } else {
                null
            }
        }
    };

    open fun updateAllowToggle(context: Context, allow: Boolean) {}
    open fun getAllowToggle(context: Context): Boolean? {
        return null
    }

    companion object {
        private const val PERMISSION_PREFERENCES_FILE = "RecorderPermissionSettings"
        private const val ALLOW_MOTION_KEY = "AllowMotionSensor"
    }
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
    private lateinit var permissionPage: PermissionPageType

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPermissionPageBinding.inflate(inflater, container, false)
        val permissionPageString = arguments?.getString(KEY_PERMISSION_PAGE) ?: throw IllegalArgumentException()
        permissionPage = PermissionPageType.valueOf(permissionPageString)
        binding.details.configureView(requireContext(), permissionPage) { group, checkedId ->
            (requireParentFragment() as PermissionsFragment).binding.nextButton.isEnabled =
                checkedId != -1
        }
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        val nextButtonEnabled = !permissionPage.showToggle || binding.details.changeToggle.checkedRadioButtonId != -1
        (requireParentFragment() as PermissionsFragment).binding.nextButton.isEnabled = nextButtonEnabled
    }



}

fun PermissionDetailsBinding.configureView(context: Context, permissionPage: PermissionPageType, onCheckedChangeListener: RadioGroup.OnCheckedChangeListener? = null) {
    logo.setImageResource(permissionPage.iconResource)
    header.text = context.getString(permissionPage.headerStringIdentifier)
    body.text = context.getText(permissionPage.bodyStringIdentifier)
    if (permissionPage.showToggle) {
        changeToggle.visibility = View.VISIBLE
        permissionPage.getAllowToggle(context)?.let { allowPermission ->
            allowRadio.isChecked = allowPermission
            disallowRadio.isChecked = !allowPermission
        }
        changeToggle.setOnCheckedChangeListener { buttonView, checkedId ->
            permissionPage.updateAllowToggle(context, checkedId == R.id.allow_radio )
            onCheckedChangeListener?.onCheckedChanged(buttonView, checkedId)
        }
    }
}
