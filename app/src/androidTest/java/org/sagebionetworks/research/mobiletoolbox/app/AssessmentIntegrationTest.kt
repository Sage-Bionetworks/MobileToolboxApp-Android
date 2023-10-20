@file:OptIn(ExperimentalCoroutinesApi::class)

package org.sagebionetworks.research.mobiletoolbox.app

import android.Manifest
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.fasterxml.jackson.databind.ObjectMapper
import com.networknt.schema.JsonSchemaFactory
import com.networknt.schema.SpecVersion
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.jsonObject
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import org.sagebionetworks.assessmentmodel.navigation.CustomNodeStateProvider
import org.sagebionetworks.bridge.assessmentmodel.upload.AssessmentResultArchiveUploader
import org.sagebionetworks.bridge.kmm.shared.cache.ResourceResult
import org.sagebionetworks.bridge.kmm.shared.models.Study
import org.sagebionetworks.bridge.kmm.shared.repo.AdherenceRecordRepo
import org.sagebionetworks.bridge.kmm.shared.repo.AuthenticationRepository
import org.sagebionetworks.research.mobiletoolbox.app.ui.login.LoginActivity
import org.sagebionetworks.research.mobiletoolbox.app.ui.login.ParticipantIdSignInFragment
import org.sagebionetworks.research.mobiletoolbox.app.ui.login.PermissionPageType
import org.sagebionetworks.research.mobiletoolbox.app.ui.login.WelcomeScreenFragment
import org.sagebionetworks.research.mobiletoolbox.app.ui.today.TodayRecyclerViewAdapter
import java.net.URI
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

// TODO: Figure out a replacement assessment so we can re-enable this test -nbrown 10/20/2023
/*
@RunWith(AndroidJUnit4::class)
class AssessmentIntegrationTest : KoinComponent {

    companion object : KoinComponent {

        private val authRepo: AuthenticationRepository by inject()
        private val archiveUploader: AssessmentResultArchiveUploader by inject()
        private val adherenceRecordRepo: AdherenceRecordRepo by inject()

        @BeforeClass
        @JvmStatic fun setup() {
            runBlocking {
//                val mtbAppNodeStateProvider: CustomNodeStateProvider = get(named("mtb-northwestern"))
//                (mtbAppNodeStateProvider as MtbAppNodeStateProvider).deleteAllData()
                authRepo.signOut()
            }
        }

        const val STUDY_ID = "bfwfhn"

    }

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()


    @get:Rule
    val mRuntimePermissionRule: GrantPermissionRule = if (Build.VERSION.SDK_INT >= 33) {
        GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS, Manifest.permission.ACCESS_COARSE_LOCATION)
    } else {
        GrantPermissionRule.grant(Manifest.permission.ACCESS_COARSE_LOCATION)
    }


    @get:Rule
    val composeTestRule = createAndroidComposeRule(MtbMainActivity::class.java)


    // Disabling Flanker test as NU measures have been removed -nbrown 10/19/2023
    // Leaving code as example for setting up test with a different assessment
    @Ignore
    fun testFlankerAssessment() {
        runTest {
            val targetContext = InstrumentationRegistry.getInstrumentation().targetContext

            val participantId = "605291"

            // Select study screen
            onView(withId(R.id.studyIdInput)).perform(typeText(STUDY_ID), closeSoftKeyboard())
            composeTestRule.onNodeWithText("Next").performClick()

            //TODO: Figure out idling resources to wait for studyInfo to load -nbrown 03/21/23
            // https://developer.android.com/training/testing/espresso/idling-resource
            Thread.sleep(5000)

            val loginActivity = (FlankerTest.getTopActivity() as FragmentActivity)
            val signInFragment = loginActivity.supportFragmentManager.fragments.first { it is ParticipantIdSignInFragment } as ParticipantIdSignInFragment
            val loginViewModel = signInFragment.viewModel
            val studyLoadedLatch = CountDownLatch(1)
            val studyLoadedObserver = Observer<ResourceResult<Study>> {
                if (it is ResourceResult.Success) {
                    studyLoadedLatch.countDown()
                }
            }
            loginViewModel.studyLiveData.observeForever(studyLoadedObserver)

            // Login screen
            onView(withId(R.id.participantIdInput)).perform(
                typeText(participantId),
                closeSoftKeyboard()
            )
            composeTestRule.onNodeWithText("Login").performClick()

            studyLoadedLatch.await(20, TimeUnit.SECONDS)
            loginViewModel.studyLiveData.removeObserver(studyLoadedObserver)

            val session = authRepo.session()
            assertNotNull(session)
            assertTrue(session!!.authenticated)

//            takeScreenshot()
//                .writeToTestStorage("welcome_screen")

            // Don't allow motion sensor since emulator behaves strangely
            PermissionPageType.MOTION_PAGE.updateAllowToggle(targetContext, false)

            // Welcome screen
            //Espresso call with scrollTo: onView(withId(R.id.next_button)).perform(scrollTo(), click())
            // has been fragile, call onNextClicked() on fragment directly instead -nbrown 5/30/23
            val activity = FlankerTest.getTopActivity() as FragmentActivity
            val welcomeScreenFragment = activity.supportFragmentManager.fragments.first { it is WelcomeScreenFragment } as WelcomeScreenFragment
            InstrumentationRegistry.getInstrumentation().runOnMainSync {
                welcomeScreenFragment.onNextClicked()
            }

            // Privacy notice screens
            composeTestRule.onNodeWithText("Next").performClick()
            composeTestRule.onNodeWithText("Next").performClick()
            composeTestRule.onNodeWithText("Next").performClick()
            // Permissions screens
            composeTestRule.onNodeWithText("Next").performClick()
            composeTestRule.onNodeWithText("Next").performClick()

            composeTestRule.onNodeWithText("Next").performClick()

            //Should now be on today screen

            //TODO: Figure out idling resources to wait for schedule to load on today screen -nbrown 03/21/23
            Thread.sleep(10000)

            // Verify that there are no pending uploads
            assertFalse(archiveUploader.uploadRequester.pendingUploads)

            // Watch the pending uploads Flow so we can verify that results were saved and then uploaded
            val uploadLatch = CountDownLatch(1)
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
                        uploadLatch.countDown()
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
            val pair = FlankerTest.runFlanker()
            val assessmentInstanceGuid = pair.first
            val assessmentResult = pair.second

            //Wait for uplaods to succeed
            uploadLatch.await(2, TimeUnit.MINUTES)
            // Check that upload succeeded
            assertTrue(uploadSuccess)

            // Give time for uploadComplete call to finish
            Thread.sleep(10000)
            assertFalse(archiveUploader.uploadRequester.pendingUploads)

            // Check adherence record
            adherenceRecordRepo.loadRemoteAdherenceRecords(STUDY_ID)
            val recordsMap = adherenceRecordRepo.getAllCachedAdherenceRecords(STUDY_ID).first()
            val records = recordsMap[assessmentInstanceGuid]
            assertNotNull(records)
            val adherenceRecord = records!!.first { it.startedOn == assessmentResult.startDateTime }
            assertNotNull(adherenceRecord.uploadedOn)
            assertEquals(assessmentResult.endDateTime, adherenceRecord.finishedOn)
            assertNotNull(adherenceRecord.clientData)
            assertTrue(adherenceRecord.clientData!!.jsonObject.containsKey("osName"))

        }

    }

}

fun validateJson(jsonString: String, schemaUrl: String) {
    val jsonSchema = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7).getSchema(
        URI(schemaUrl)
    )
    jsonSchema.initializeValidators()
    val jsonNode = ObjectMapper().readTree(jsonString)
    val errors = jsonSchema.validate(jsonNode)
    Assert.assertTrue(errors.toString(), errors.isEmpty())
}
*/
