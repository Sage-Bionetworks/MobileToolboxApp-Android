package org.sagebionetworks.research.mobiletoolbox.app

import android.app.Activity
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import org.junit.Rule
import org.junit.runner.RunWith
import org.koin.core.component.KoinComponent
import org.sagebionetworks.assessmentmodel.navigation.FinishedReason
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class FlankerTest : KoinComponent {

    companion object : KoinComponent {

        fun runFlanker() {
            // Run Flanker assessment, assumes this is first time through
            Espresso.onView(ViewMatchers.withId(R.id.welcome_navigation_black_button)).perform(ViewActions.click())
            Thread.sleep(1000)
            Espresso.onView(ViewMatchers.withId(R.id.navigator_next_button)).perform(ViewActions.click())
            Thread.sleep(1000)
            Espresso.onView(ViewMatchers.withId(R.id.navigator_next_button)).perform(ViewActions.click())
            Thread.sleep(1000)
            Espresso.onView(ViewMatchers.withId(R.id.navigator_next_button)).perform(ViewActions.click())
            Thread.sleep(1000)
            Espresso.onView(ViewMatchers.withId(R.id.right_button)).perform(ViewActions.click())
            Thread.sleep(3000)
            Espresso.onView(ViewMatchers.withId(R.id.flanker_button_right)).perform(ViewActions.click())
            Thread.sleep(3000)
            Espresso.onView(ViewMatchers.withId(R.id.flanker_response_navigation_button)).perform(ViewActions.click())
            Thread.sleep(3000)
            Espresso.onView(ViewMatchers.withId(R.id.flanker_button_left)).perform(ViewActions.click())
            Thread.sleep(3000)
            Espresso.onView(ViewMatchers.withId(R.id.flanker_response_navigation_button)).perform(ViewActions.click())
            Thread.sleep(3000)
            Espresso.onView(ViewMatchers.withId(R.id.flanker_button_right)).perform(ViewActions.click())
            Thread.sleep(3000)
            Espresso.onView(ViewMatchers.withId(R.id.flanker_response_navigation_button)).perform(ViewActions.click())
            Thread.sleep(3000)
            Espresso.onView(ViewMatchers.withId(R.id.flanker_button_right)).perform(ViewActions.click())
            Thread.sleep(3000)
            Espresso.onView(ViewMatchers.withId(R.id.flanker_response_navigation_button)).perform(ViewActions.click())
            Thread.sleep(3000)
            Espresso.onView(ViewMatchers.withId(R.id.flanker_button_left)).perform(ViewActions.click())
            Thread.sleep(3000)
            Espresso.onView(ViewMatchers.withId(R.id.flanker_response_navigation_button)).perform(ViewActions.click())
            Thread.sleep(3000)
            Espresso.onView(ViewMatchers.withId(R.id.right_button)).perform(ViewActions.click())
            // At this point we no longer need to interact with the measure to get it to complete, just have to wait
            // Could add some random button clicks to vary the data

            Thread.sleep(60000)

            val activity = getTopActivity() as MtbAssessmentActivity
            val viewModel = activity.viewModel as MtbRootAssessmentViewModel

            // Wait for measure to complete and handleReadyToSave to be called
            val saveCalled = getValue(viewModel.assessmentReadyToSave, timeoutSeconds = 300)
            assertEquals(FinishedReason.Complete, saveCalled)

            // Wait for save code to complete
            assertNotNull(viewModel.saveJob)
            if (!viewModel.saveJob!!.isCompleted) {
                val saveJobLatch = CountDownLatch(1)
                viewModel.saveJob?.invokeOnCompletion {
                    saveJobLatch.countDown()
                }
                saveJobLatch.await(1, TimeUnit.MINUTES)
            }
            assertTrue(viewModel.saveJob?.isCompleted == true)

            // At this point the zip file should be written to the upload queue and the upload worker should be starting

            // Finish the Assessment
            Espresso.onView(ViewMatchers.withId(R.id.feedback_navigation_btn)).perform(ViewActions.click())
        }

        fun getTopActivity(): Activity? {
            var activity: Activity? = null
            InstrumentationRegistry.getInstrumentation().runOnMainSync {
                val resumedActivities = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(
                    Stage.RESUMED)
                if (resumedActivities.iterator().hasNext()) {
                    resumedActivities.iterator().next()?.let {
                        activity = it
                    }
                }
            }
            return activity
        }

    }


    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

}