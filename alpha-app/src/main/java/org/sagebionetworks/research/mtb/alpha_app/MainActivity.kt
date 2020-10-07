package org.sagebionetworks.research.mtb.alpha_app

import android.os.Bundle
import dagger.android.support.DaggerAppCompatActivity
import org.sagebionetworks.research.mtb.alpha_app.ui.main.MainFragment

class MainActivity : DaggerAppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MainFragment.newInstance())
                .commitNow()
        }
    }
//    @Inject
//    lateinit var bridgeAccessViewModelFactory: BridgeAccessViewModel.Factory
//    val bridgeAccessViewModel by viewModels<BridgeAccessViewModel>() {
//        bridgeAccessViewModelFactory
//    }

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//
//        bridgeAccessViewModel.bridgeAccessStatus.observe(
//            this,
//            Observer<Resource<BridgeAccessState>> {
//                BridgeAccessStrategy.handle(it, this)
//            })
//    }
//
//    override fun onResume() {
//        super.onResume()
//
//        bridgeAccessViewModel.checkAccess()
//    }
//
//    override fun onAccessGranted() {
//        TODO("Not yet implemented")
//    }
//
//    override fun onErrored(state: BridgeAccessState, message: String?) {
//        TODO("Not yet implemented")
//    }
//
//    override fun onLoading(state: BridgeAccessState) {
//        TODO("Not yet implemented")
//    }
//
//    override fun onRequireAppUpgrade() {
//        TODO("Not yet implemented")
//    }
//
//    override fun onRequireAuthentication() {
//        TODO("Not yet implemented")
//    }
//
//    override fun onRequireConsent() {
//        TODO("Not yet implemented")
//    }
}
/**
package org.sagebionetworks.research.mtb.alpha_app

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import dagger.android.support.DaggerAppCompatActivity
import org.sagebionetworks.bridge.android.access.BridgeAccessState
import org.sagebionetworks.bridge.android.access.BridgeAccessStrategy
import org.sagebionetworks.bridge.android.access.BridgeAccessViewModel
import org.sagebionetworks.bridge.android.access.Resource
import org.sagebionetworks.research.mtb.alpha_app.ui.main.MainFragment
import org.sagebionetworks.research.mtb.alpha_app.ui.researcher_sign_in.ResearcherSignInFragment
import org.sagebionetworks.research.mtb.alpha_app.ui.task_list.ShowTaskListFragment
import javax.inject.Inject

class MainActivity : DaggerAppCompatActivity(), BridgeAccessStrategy {


@Inject
lateinit var bridgeAccessViewModelFactory: BridgeAccessViewModel.Factory
val bridgeAccessViewModel by viewModels<BridgeAccessViewModel>() {
bridgeAccessViewModelFactory
}


override fun onCreate(savedInstanceState: Bundle?) {
super.onCreate(savedInstanceState)
setContentView(R.layout.activity_main)
if (savedInstanceState == null) {
supportFragmentManager.beginTransaction()
.replace(R.id.container, MainFragment.newInstance())
.commitNow()
}
bridgeAccessViewModel.bridgeAccessStatus.observe(
this,
Observer<Resource<BridgeAccessState>> {
BridgeAccessStrategy.handle(it, this)
})
}

override fun onResume() {
super.onResume()

bridgeAccessViewModel.checkAccess()
}

override fun onAccessGranted() {
supportFragmentManager.beginTransaction()
.replace(R.id.container, ShowTaskListFragment.newInstance())
.commitNow()
}

override fun onErrored(state: BridgeAccessState, message: String?) {
TODO("Not yet implemented")
}

override fun onLoading(state: BridgeAccessState) {
TODO("Not yet implemented")
}

override fun onRequireAppUpgrade() {
TODO("Not yet implemented")
}

override fun onRequireAuthentication() {
supportFragmentManager.beginTransaction()
.replace(R.id.container, ResearcherSignInFragment.newInstance())
.commitNow()
}

override fun onRequireConsent() {
TODO("Not yet implemented")
}
}**/