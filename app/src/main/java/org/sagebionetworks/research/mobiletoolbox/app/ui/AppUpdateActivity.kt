package org.sagebionetworks.research.mobiletoolbox.app.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.sagebionetworks.assessmentmodel.presentation.ui.theme.SageSurveyTheme
import org.sagebionetworks.research.mobiletoolbox.app.ui.login.UpdateNowUi

class AppUpdateActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SageSurveyTheme {
                UpdateNowUi() {
                    launchGooglePlayForUpdate()
                }
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        //Override to block going back
    }

    private fun launchGooglePlayForUpdate() {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(
                "https://play.google.com/store/apps/details?id=org.sagebionetworks.research.mobiletoolbox.app")
            setPackage("com.android.vending")
        }
        startActivity(intent)
    }

}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    SageSurveyTheme {
        UpdateNowUi() {

        }
    }
}