package org.sagebionetworks.research.mtb.alpha_app.ui.main

import org.sagebionetworks.bridge.android.access.BridgeAccessFragment
import org.sagebionetworks.research.mtb.alpha_app.R
import org.sagebionetworks.research.mtb.alpha_app.ui.researcher_sign_in.ResearcherSignInFragment
import org.sagebionetworks.research.mtb.alpha_app.ui.task_list.ShowTaskListFragment
import org.slf4j.LoggerFactory

class MainFragment : BridgeAccessFragment() {

    companion object {
        fun newInstance() = MainFragment()

        private val LOGGER = LoggerFactory.getLogger(MainFragment::class.java)
    }

    override fun onAccessGranted() {
        childFragmentManager.beginTransaction()
            .replace(R.id.container, ShowTaskListFragment())
            .commit()
    }

    override fun onRequireAuthentication() {
        LOGGER.debug("Authentication required: showing ResearcherSignInFragment")

        val fragment = childFragmentManager.findFragmentByTag("ResearcherSignInFragment")
        if (fragment?.isVisible == true) {
            return
        }
        childFragmentManager.beginTransaction()
            .replace(
                R.id.container,
                ResearcherSignInFragment.newInstance(),
                "ResearcherSignInFragment"
            )
            .commit()
    }

    override fun onRequireConsent() {
        LOGGER.error("Missing consent. Currently unhandled")
        TODO("not implemented")
    }
}
