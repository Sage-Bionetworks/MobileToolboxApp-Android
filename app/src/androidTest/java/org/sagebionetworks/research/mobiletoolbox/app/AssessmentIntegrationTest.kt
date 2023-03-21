@file:OptIn(ExperimentalCoroutinesApi::class)

package org.sagebionetworks.research.mobiletoolbox.app

import android.Manifest
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.hamcrest.core.AllOf.allOf
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.sagebionetworks.bridge.assessmentmodel.upload.AssessmentResultArchiveUploader
import org.sagebionetworks.bridge.kmm.shared.repo.AuthenticationRepository
import org.sagebionetworks.research.mobiletoolbox.app.ui.today.TodayRecyclerViewAdapter


@RunWith(AndroidJUnit4::class)
class AssessmentIntegrationTest : KoinComponent {

    companion object : KoinComponent {

        private val authRepo: AuthenticationRepository by inject()
        private val archiveUploader: AssessmentResultArchiveUploader by inject()

        @BeforeClass
        @JvmStatic fun setup() {
            runBlocking {
                // TODO: Need clearData functionality from NWU for repeated test runs -nbrown 03/21/23
                authRepo.signOut()
            }
        }

        const val STUDY_ID = "bfwfhn"

    }

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mRuntimePermissionRule = GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS)

    @get:Rule
    val composeTestRule = createAndroidComposeRule(MtbMainActivity::class.java)


    @Test
    fun testFlankerAssessment() {
        runTest {
            val participantId = "605291"

            // Select study screen
            onView(withId(R.id.studyIdInput)).perform(typeText(STUDY_ID), closeSoftKeyboard())
            composeTestRule.onNodeWithText("Next").performClick()

            //TODO: Figure out idling resources to wait for studyInfo to load -nbrown 03/21/23
            // https://developer.android.com/training/testing/espresso/idling-resource
            Thread.sleep(5000)

            // Login screen
            onView(withId(R.id.participantIdInput)).perform(
                typeText(participantId),
                closeSoftKeyboard()
            )
            composeTestRule.onNodeWithText("Login").performClick()

            //TODO: Figure out idling resources to wait for login to complete -nbrown 03/21/23
            Thread.sleep(5000)
            // Welcome screen
            onView(withId(R.id.next_button)).perform(click())
            // Privacy notice screens
            composeTestRule.onNodeWithText("Next").performClick()
            composeTestRule.onNodeWithText("Next").performClick()
            composeTestRule.onNodeWithText("Next").performClick()
            // Permissions screens
            composeTestRule.onNodeWithText("Next").performClick()
            composeTestRule.onNodeWithText("Next").performClick()
            // Click allow motion so there will be a background recorder running
            onView(allOf(withId(R.id.allow_radio), isCompletelyDisplayed())).perform(click())
            composeTestRule.onNodeWithText("Next").performClick()

            //Should now be on today screen

            //TODO: Figure out idling resources to wait for schedule to load on today screen -nbrown 03/21/23
            Thread.sleep(5000)

            // Verify that there are no pending uploads
            assertFalse(archiveUploader.uploadRequester.pendingUploads)

            // Watch the pending uploads Flow so we can verify that results were saved and then uploaded
            var uploadQueued = false
            var uploadSuccess = false
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                archiveUploader.uploadRequester.getPendingFileUploadsFlow().collect { uploadList ->
                    if (uploadList.isNotEmpty()) {
                        // Results were written to upload queue
                        uploadQueued = true
                    }
                    if (uploadQueued && uploadList.isEmpty()) {
                        // Uploads were successfully processed and saved to Bridge
                        uploadSuccess = true
                    }
                }
            }

            // Launch first assessment in session, which should be Flanker
            onView(withId(R.id.list)).perform(
                // Header is at position 0, session is at position 1, first assessment is at position 2
                RecyclerViewActions.actionOnItemAtPosition<TodayRecyclerViewAdapter.AssessmentViewHolder>(
                    2,
                    click()
                )
            )
            // Run through flanker
            FlankerTest.runFlanker()

            //TODO: Figure better approach to give time for uploads to complete -nbrown 03/21/23
            Thread.sleep(5000)

            // TODO: Check adherence records -nbrown 03/21/23

            // Check that upload succeeded
            assertTrue(uploadSuccess)

        }

    }



}