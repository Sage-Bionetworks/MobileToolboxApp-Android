package org.sagebionetworks.research.mobiletoolbox.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.koin.android.ext.android.inject
import org.sagebionetworks.bridge.kmm.shared.repo.AuthenticationRepository
import org.sagebionetworks.research.mobiletoolbox.app.databinding.ActivityMtbMainBinding
import org.sagebionetworks.research.mobiletoolbox.app.ui.login.LoginActivity

class MtbMainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMtbMainBinding
    private val authenticationRepository: AuthenticationRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMtbMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_mtb_main)
        navView.setupWithNavController(navController)
    }

    override fun onResume() {
        super.onResume()
        if (!authenticationRepository.isAuthenticated()) {
            // Show sign-in screen
            val launchIntent = Intent(this, LoginActivity::class.java)
                .setData(intent.data)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)

            startActivity(launchIntent)
        }
    }

}