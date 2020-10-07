package org.sagebionetworks.research.mtb.alpha_app.di

import dagger.Module
import dagger.android.ContributesAndroidInjector
import org.sagebionetworks.bridge.android.di.BridgeStudyScope
import org.sagebionetworks.research.mtb.alpha_app.MainActivity
import org.sagebionetworks.research.mtb.alpha_app.ui.main.MainFragment
import org.sagebionetworks.research.mtb.alpha_app.ui.researcher_sign_in.ResearcherSignInFragment
import org.sagebionetworks.research.mtb.alpha_app.ui.task_list.ShowTaskListFragment

@Module
abstract class MtbAlphaAppModule {
    @ContributesAndroidInjector
//    @BridgeStudyScope
    abstract fun contributesMainActivityInjector(): MainActivity


    @ContributesAndroidInjector
    abstract fun contributesMainFragmentInjector(): MainFragment

    @ContributesAndroidInjector
    abstract fun contributesTaskListFragmentInjector(): ShowTaskListFragment

    @ContributesAndroidInjector
    abstract fun contributesResearcherSignInFragmentInjector(): ResearcherSignInFragment
}