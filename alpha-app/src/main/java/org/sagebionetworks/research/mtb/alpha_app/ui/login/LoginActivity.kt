package org.sagebionetworks.research.mtb.alpha_app.ui.login

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import org.sagebionetworks.research.mtb.alpha_app.R

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, SelectStudyFragment.newInstance())
                .commitNow()
        }
    }
}