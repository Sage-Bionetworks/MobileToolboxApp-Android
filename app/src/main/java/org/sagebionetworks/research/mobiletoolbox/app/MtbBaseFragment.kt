package org.sagebionetworks.research.mobiletoolbox.app

import android.content.Intent
import androidx.fragment.app.Fragment
import org.koin.android.ext.android.inject
import org.sagebionetworks.bridge.kmm.shared.repo.AuthenticationRepository
import org.sagebionetworks.research.mobiletoolbox.app.ui.login.LoginActivity

abstract class MtbBaseFragment : Fragment() {

    private val authenticationRepository: AuthenticationRepository by inject()

    override fun onResume() {
        super.onResume()
        if (!authenticationRepository.isAuthenticated()) {
            // Show sign-in screen
            val launchIntent = Intent(requireContext(), LoginActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(launchIntent)
        }
    }

}