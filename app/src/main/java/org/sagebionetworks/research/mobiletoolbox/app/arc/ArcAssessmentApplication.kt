package org.sagebionetworks.research.mobiletoolbox.app.arc

import android.content.res.Configuration
import androidx.multidex.MultiDexApplication
import edu.wustl.Arc.app.arc.model.ArcMtbStateMachine
import edu.wustl.arc.core.Config
import edu.wustl.arc.study.Study
import edu.wustl.arc.study.TestVariant
import edu.wustl.arc.ui.BottomNavigationView
import org.koin.core.component.KoinComponent
import org.sagebionetworks.assessmentmodel.navigation.BranchNodeState

open class ArcAssessmentApplication : MultiDexApplication(), KoinComponent {

    override fun onCreate() {
        super.onCreate()

        BottomNavigationView.shouldShowEarnings = false
        Config.CHOOSE_LOCALE = false
        Config.CHECK_CONTACT_INFO = true
        Config.CHECK_SESSION_INFO = true
        Config.CHECK_PROGRESS_INFO = true
        Config.ENABLE_VIGNETTES = true
        Config.IS_REMOTE = true
        Config.ENABLE_SIGNATURES = true
        Config.USE_HELP_SCREEN = false
        Config.TEST_VARIANT_GRID = TestVariant.Grid.V2
        Config.TEST_VARIANT_PRICE = TestVariant.Price.Original

        // Initialize library
        edu.wustl.arc.core.Application.initialize(this) {
            Study.getInstance().registerStateMachine(
                ArcMtbStateMachine::class.java
            )
        }
        Study.getStateMachine().initialize()
        edu.wustl.arc.core.Application.getInstance().localeOptions = ArrayList()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        edu.wustl.arc.core.Application.getInstance()?.onConfigurationChanged(newConfig)
    }
}