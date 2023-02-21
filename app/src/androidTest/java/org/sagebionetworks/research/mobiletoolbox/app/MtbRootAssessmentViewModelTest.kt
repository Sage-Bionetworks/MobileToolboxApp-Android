package org.sagebionetworks.research.mobiletoolbox.app

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.serialization.decodeFromString
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.sagebionetworks.assessmentmodel.AssessmentRegistryProvider
import org.sagebionetworks.assessmentmodel.AssessmentResultCache
import org.sagebionetworks.assessmentmodel.navigation.CustomNodeStateProvider
import org.sagebionetworks.assessmentmodel.navigation.FinishedReason
import org.sagebionetworks.assessmentmodel.navigation.SaveResults
import org.sagebionetworks.assessmentmodel.serialization.AssessmentInfoObject
import org.sagebionetworks.assessmentmodel.serialization.AssessmentPlaceholderObject
import org.sagebionetworks.bridge.assessmentmodel.upload.AssessmentResultArchiveUploader
import org.sagebionetworks.bridge.kmm.shared.models.AdherenceRecord
import org.sagebionetworks.bridge.kmm.shared.repo.AdherenceRecordRepo
import org.sagebionetworks.bridge.kmm.shared.repo.AuthenticationRepository
import org.sagebionetworks.research.mobiletoolbox.app.MtbRootAssessmentViewModel
import org.sagebionetworks.research.mobiletoolbox.app.recorder.RecorderRunner
import org.sagebionetworks.research.mobiletoolbox.app.recorder.model.recorderConfigJsonCoder
import org.sagebionetworks.research.mobiletoolbox.app.ui.login.PermissionPageType
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class MtbRootAssessmentViewModelTest : KoinComponent {

    //Documentation if we want to insert different implementations for test as needed
    //https://insert-koin.io/docs/reference/koin-android/instrumented-testing

    private val archiveUploader: AssessmentResultArchiveUploader by inject()
    private val adherenceRecordRepo: AdherenceRecordRepo by inject()
    private val recorderRunnerFactory: RecorderRunner.RecorderRunnerFactory by inject()
    private val assessmentResultCache: AssessmentResultCache by inject()
    private val authRepo: AuthenticationRepository by inject()
    val nodeStateProvider: CustomNodeStateProvider? by inject()
    val registryProvider: AssessmentRegistryProvider by inject()


    val recorderConfig = """[
   {
      "recorder":{
         "identifier":"motion",
         "type":"motion"
      },
      "enabledByStudyClientData":true,
      "disabledByAppForTaskIdentifiers":[
         
      ],
      "services":[
         
      ]
   },
   {
      "recorder":{
         "identifier":"microphone",
         "type":"microphone"
      },
      "enabledByStudyClientData":true,
      "disabledByAppForTaskIdentifiers":[
         "MTB Spelling Form 1",
         "Picture Sequence MemoryV1",
         "MyCogMobileV1",
         "MyCogMobilePSM"
      ],
      "services":[
         
      ]
   }
]"""

    lateinit var targetContext: Context

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        targetContext = InstrumentationRegistry.getInstrumentation().targetContext
    }

    @After
    fun cleanUp() {
        runBlocking {
            authRepo.signOut()
        }
    }

    /**
     * This test starts an assessment, exits early, and then verifies that the results are archived
     * and saved to the upload queue. It also checks that the adherence record is created correctly.
     * This test is executed on an Android emulator with the real app. Real web calls will be attempted,
     * but by being signed-out all authenticated calls will fail with a 401. This allows us to verify
     * the client side logic without creating any data in Bridge.
     */
    @Test
    fun testSavingAssessmentResult() {
        runBlocking {
            authRepo.signOut()
        }
        // Give permission to collect motion so we have a recorder running
        PermissionPageType.MOTION_PAGE.updateAllowToggle(targetContext, true)

        // Verify that there are no pending uploads
        assertFalse(archiveUploader.uploadRequester.pendingUploads)

        val studyId = "androidTestStudyId"
        // TODO: Figure out best assessment to use for testing or testing multiple -nbrown 2/20/23
        val assessmentInfo = AssessmentInfoObject("Flanker Inhibitory Control")
        val assessmentPlaceholder = AssessmentPlaceholderObject(assessmentInfo.identifier, assessmentInfo)
        val assessmentInstanceId = "testInstanceGuid"
        val eventTimestamp = Clock.System.now()
        val adherenceRecord = AdherenceRecord(assessmentInstanceId, eventTimestamp.toString())

        val viewModel = MtbRootAssessmentViewModel(
            assessmentPlaceholder = assessmentPlaceholder,
            registryProvider = registryProvider,
            nodeStateProvider = nodeStateProvider,
            assessmentResultCache = assessmentResultCache,
            assessmentInstanceId = assessmentInstanceId,
            sessionExpiration = null,
            archiveUploader = archiveUploader,
            adherenceRecordRepo = adherenceRecordRepo,
            adherenceRecord = adherenceRecord,
            recorderRunnerFactory = recorderRunnerFactory,
            studyId = studyId)

        val branchNodeState = getValue(viewModel.assessmentLoadedLiveData)

        assertNotNull(branchNodeState)
        branchNodeState?.rootNodeController = viewModel

        recorderRunnerFactory.withConfig(recorderConfigJsonCoder.decodeFromString(recorderConfig))
        viewModel.startRecorderRunner()

        //TODO: Figure out navigating through assessment to generate more results -nbrown 2/20/23
        branchNodeState?.exitEarly(
            FinishedReason.Incomplete(
                SaveResults.Now,
                markFinished = false,
                declined = true
            )
        )

        assertNotNull(viewModel.saveJob)
        val saveJobLatch = CountDownLatch(1)
        viewModel.saveJob?.invokeOnCompletion {
            saveJobLatch.countDown()
        }

        saveJobLatch.await(5, TimeUnit.SECONDS)
        // Check that saving job is done
        assertTrue(viewModel.saveJob?.isCompleted == true)

        // Check that upload got written to disk
        assertTrue(archiveUploader.uploadRequester.pendingUploads)

        // Check the adherence record
        runBlocking {
            val recordsMap = adherenceRecordRepo.getAllCachedAdherenceRecords(studyId).first()
            assertEquals(1, recordsMap.size)
            val records = recordsMap.entries.first().value
            assertEquals(1, records.size)
            val record = records[0]
            assertTrue(record.declined)
            assertNotNull(record.startedOn)
            assertNull(record.finishedOn)
        }

    }
}

@Throws(InterruptedException::class)
internal fun <T> getValue(liveData: LiveData<T>): T? {
    var data: T? = null
    val latch = CountDownLatch(1)
    val observer = object : Observer<T> {
        override fun onChanged(changedData: T?) {
            data = changedData
            latch.countDown()
            liveData.removeObserver(this)
        }
    }
    liveData.observeForever(observer)
    latch.await(2, TimeUnit.SECONDS)
    liveData.removeObserver(observer)
    return data
}