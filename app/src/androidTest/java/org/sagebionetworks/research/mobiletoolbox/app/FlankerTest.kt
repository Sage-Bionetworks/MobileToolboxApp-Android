package org.sagebionetworks.research.mobiletoolbox.app

import android.app.Activity
import android.util.Log
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
import kotlinx.serialization.encodeToString
import org.junit.Rule
import org.junit.runner.RunWith
import org.koin.core.component.KoinComponent
import org.sagebionetworks.assessmentmodel.AssessmentResult
import org.sagebionetworks.assessmentmodel.navigation.FinishedReason
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class FlankerTest : KoinComponent {

    companion object : KoinComponent {

        // Code no longer runs as NU measures have been removed -nbrown 10/19/2023
        // Leaving code as example for setting up test with a different assessment
        fun runFlanker() : Pair<String, AssessmentResult>{
            // Run Flanker assessment, assumes this is first time through
//            Espresso.onView(ViewMatchers.withId(R.id.welcome_navigation_black_button)).perform(ViewActions.click())
//            Thread.sleep(1000)
//            Espresso.onView(ViewMatchers.withId(R.id.navigator_next_button)).perform(ViewActions.click())
//            Thread.sleep(1000)
//            Espresso.onView(ViewMatchers.withId(R.id.navigator_next_button)).perform(ViewActions.click())
//            Thread.sleep(1000)
//            Espresso.onView(ViewMatchers.withId(R.id.navigator_next_button)).perform(ViewActions.click())
//            Thread.sleep(1000)
//            Espresso.onView(ViewMatchers.withId(R.id.right_button)).perform(ViewActions.click())
//            Thread.sleep(3000)
//            Espresso.onView(ViewMatchers.withId(R.id.flanker_button_right)).perform(ViewActions.click())
//            Thread.sleep(3000)
//            Espresso.onView(ViewMatchers.withId(R.id.flanker_response_navigation_button)).perform(ViewActions.click())
//            Thread.sleep(3000)
//            Espresso.onView(ViewMatchers.withId(R.id.flanker_button_left)).perform(ViewActions.click())
//            Thread.sleep(3000)
//            Espresso.onView(ViewMatchers.withId(R.id.flanker_response_navigation_button)).perform(ViewActions.click())
//            Thread.sleep(3000)
//            Espresso.onView(ViewMatchers.withId(R.id.flanker_button_right)).perform(ViewActions.click())
//            Thread.sleep(3000)
//            Espresso.onView(ViewMatchers.withId(R.id.flanker_response_navigation_button)).perform(ViewActions.click())
//            Thread.sleep(3000)
//            Espresso.onView(ViewMatchers.withId(R.id.flanker_button_right)).perform(ViewActions.click())
//            Thread.sleep(3000)
//            Espresso.onView(ViewMatchers.withId(R.id.flanker_response_navigation_button)).perform(ViewActions.click())
//            Thread.sleep(3000)
//            Espresso.onView(ViewMatchers.withId(R.id.flanker_button_left)).perform(ViewActions.click())
//            Thread.sleep(3000)
//            Espresso.onView(ViewMatchers.withId(R.id.flanker_response_navigation_button)).perform(ViewActions.click())
//            Thread.sleep(3000)
//            Espresso.onView(ViewMatchers.withId(R.id.right_button)).perform(ViewActions.click())
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

            val assessmentInstanceGuid = viewModel.assessmentInstanceId
            val results = viewModel.assessmentNodeState?.currentResult as AssessmentResult

            val jsonCoder = viewModel.registryProvider.getJsonCoder(viewModel.assessmentPlaceholder)
            var resultJson = jsonCoder.encodeToString(results)
            Log.d("FlankerTest", resultJson)
            // Hacky workaround until Northwestern updates their schema to allow type field. -nbrown 5/26/2023
            resultJson = resultJson.replace("\"type\":\"edu.northwestern.mobiletoolbox.flanker.serialization.FlankerAssessmentResult\"," ,"")
            resultJson = resultJson.replace("\"type\":\"edu.northwestern.mobiletoolbox.common.data.UserInteraction\"," ,"")
//            validateJson(resultJson, "https://raw.githubusercontent.com/MobileToolbox/MTBfx/937cdd1bf3b09815e97b53632c58208a14255b34/JSONschema/taskData_combinedSchema.json")

            // At this point the zip file should be written to the upload queue and the upload worker should be starting

            // Finish the Assessment
//            Espresso.onView(ViewMatchers.withId(R.id.feedback_navigation_btn)).perform(ViewActions.click())
            return Pair(assessmentInstanceGuid!!, results)
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