package org.sagebionetworks.research.mtb.alpha_app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.koin.android.ext.android.inject
import org.sagebionetworks.bridge.kmm.presentation.auth.ExternalIdSignInActivity
import org.sagebionetworks.bridge.kmm.shared.repo.AuthenticationRepository
import org.sagebionetworks.research.mtb.alpha_app.ui.today.TodayFragment

class OldMainActivity :AppCompatActivity() {

    val authenticationRepository: AuthenticationRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_old_main)
    }

    override fun onResume() {
        super.onResume()
        if (!authenticationRepository.isAuthenticated()) {
            // Show sign-in screen
            val launchIntent = Intent(this, ExternalIdSignInActivity::class.java)
                .setData(intent.data)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)

            startActivity(launchIntent)
        } else {
            // Show Today screen
            if (supportFragmentManager.fragments.isEmpty()) {
                supportFragmentManager.beginTransaction().add(android.R.id.content, TodayFragment())
                    .setReorderingAllowed(true).commit()
            }
        }

    }
}