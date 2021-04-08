package org.sagebionetworks.research.mtb.alpha_app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.koin.android.ext.android.inject
import org.sagebionetworks.bridge.kmm.presentation.auth.ExternalIdSignInActivity
import org.sagebionetworks.bridge.kmm.shared.repo.AuthenticationRepository
import org.sagebionetworks.research.mtb.alpha_app.ui.AssessmentListActivity

class MainActivity :AppCompatActivity() {

    val authenticationRepository: AuthenticationRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        if (!authenticationRepository.isAuthenticated()) {
            val launchIntent = Intent(this, ExternalIdSignInActivity::class.java)
                .setData(intent.data)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)

            startActivity(launchIntent)
        } else {
            val launchIntent = Intent(this, AssessmentListActivity::class.java)
                .setData(intent.data)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)

            startActivity(launchIntent)
        }

    }
}