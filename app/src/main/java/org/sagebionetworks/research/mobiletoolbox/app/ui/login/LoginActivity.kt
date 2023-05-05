package org.sagebionetworks.research.mobiletoolbox.app.ui.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.sagebionetworks.bridge.kmm.shared.repo.AppStatus
import org.sagebionetworks.bridge.kmm.shared.repo.AuthenticationRepository
import org.sagebionetworks.research.mobiletoolbox.app.R
import org.sagebionetworks.research.mobiletoolbox.app.ui.AppUpdateActivity

class LoginActivity : AppCompatActivity() {

    private val authenticationRepository: AuthenticationRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, SelectStudyFragment.newInstance())
                .commitNow()
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                authenticationRepository.appStatus.collect { appStatus ->
                    if (appStatus == AppStatus.UNSUPPORTED) {
                        // Block the user and Notify them that they need to update the app
                        val launchIntent = Intent(this@LoginActivity, AppUpdateActivity::class.java)
                            .setData(intent.data)
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)

                        startActivity(launchIntent)
                    }
                }
            }
        }
    }
}