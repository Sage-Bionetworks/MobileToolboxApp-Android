package org.sagebionetworks.research.mobiletoolbox.app.ui.study

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import org.sagebionetworks.research.mobiletoolbox.app.databinding.ActivityPrivacyNoticeBinding

class PrivacyNoticeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrivacyNoticeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrivacyNoticeBinding.inflate(layoutInflater)
        binding.back.setOnClickListener {
            finish()
        }

        val view = binding.root
        setContentView(view)
    }
}