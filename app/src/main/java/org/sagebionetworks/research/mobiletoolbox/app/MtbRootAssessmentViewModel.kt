package org.sagebionetworks.research.mobiletoolbox.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import co.touchlab.kermit.Logger
import edu.northwestern.mobiletoolbox.common.data.MtbAssessmentResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.sagebionetworks.assessmentmodel.AssessmentPlaceholder
import org.sagebionetworks.assessmentmodel.AssessmentRegistryProvider
import org.sagebionetworks.assessmentmodel.AssessmentResult
import org.sagebionetworks.assessmentmodel.AssessmentResultCache
import org.sagebionetworks.assessmentmodel.navigation.CustomNodeStateProvider
import org.sagebionetworks.assessmentmodel.navigation.FinishedReason
import org.sagebionetworks.assessmentmodel.navigation.NodeState
import org.sagebionetworks.assessmentmodel.navigation.SaveResults
import org.sagebionetworks.assessmentmodel.presentation.RootAssessmentViewModel
import org.sagebionetworks.bridge.assessmentmodel.upload.AssessmentResultArchiveUploader
import org.sagebionetworks.bridge.kmm.shared.models.AdherenceRecord
import org.sagebionetworks.bridge.kmm.shared.repo.AdherenceRecordRepo
import org.sagebionetworks.research.mobiletoolbox.app.recorder.RecorderRunner

//This eventually can probably move down into BridgeClientKMM as a generalized BridgeAssessmentViewModel.
// Putting it here for now until we work out the details of what it needs. -nbrown 03/12/20201
class MtbRootAssessmentViewModel(
    assessmentPlaceholder: AssessmentPlaceholder,
    registryProvider: AssessmentRegistryProvider,
    nodeStateProvider: CustomNodeStateProvider?,
    assessmentResultCache: AssessmentResultCache?,
    assessmentInstanceId: String?,
    sessionExpiration: Instant?,
    private val archiveUploader: AssessmentResultArchiveUploader,
    private val adherenceRecordRepo: AdherenceRecordRepo,
    private val adherenceRecord: AdherenceRecord,
    private val recorderRunnerFactory: RecorderRunner.RecorderRunnerFactory,
    private val studyId: String
) : RootAssessmentViewModel(assessmentPlaceholder, registryProvider, nodeStateProvider, assessmentResultCache, assessmentInstanceId, sessionExpiration) {

    private var isAlreadyStarted = false
    private lateinit var recorderRunner: RecorderRunner

    fun startRecorderRunner() {
        // in TodayFragment#launchAssessment, we replaced assessmentId with taskId
        if (isAlreadyStarted) {
            Logger.i("Recorder already started, do nothing")
            return
        }
        val taskIdentifier = assessmentPlaceholder.identifier
        recorderRunner = recorderRunnerFactory.create(taskIdentifier)
        recorderRunner.start()
        isAlreadyStarted = true
    }

    override fun handleReadyToSave(reason: FinishedReason, nodeState: NodeState) {
        Logger.d("FinishedReason: ${reason.javaClass.simpleName}")
        val finishedTimeStamp = if (reason.markFinished) {
            nodeState.currentResult.endDateTime ?: Clock.System.now()
        } else {
            null
        }
        val startedTimeStamp = nodeState.currentResult.startDateTime
        adherenceRecordRepo.createUpdateAdherenceRecord(
            adherenceRecord.copy(
                startedOn = startedTimeStamp,
                finishedOn = finishedTimeStamp, declined = reason.declined
            ), studyId
        )

        if (reason.saveResult == SaveResults.Now || reason.saveResult == SaveResults.WhenSessionExpires) {
            val jsonCoder = registryProvider.getJsonCoder(assessmentPlaceholder)
            val assessmentResult = nodeState.currentResult as AssessmentResult

            val sessionExpire: Instant? = if (reason.saveResult == SaveResults.WhenSessionExpires) {
                sessionExpiration
            } else {
                null
            }

            val coroutineExceptionLogger = CoroutineExceptionHandler { coroutineContext, throwable ->
                Logger.e("Encountered coroutine exception in job ${coroutineContext[Job]}", throwable)
            }

            CoroutineScope(Dispatchers.IO)
                .launch(coroutineExceptionLogger) {
                    Logger.d("Working in thread ${Thread.currentThread().name}, job ${coroutineContext[Job]}")

                    try {

                        Logger.i("Recorders stop and await")
                        val recorderResultsDeferred = recorderRunner.stop()

                        Logger.i("Recorders stop done")

                        val recorderResults = recorderResultsDeferred.await()

                        Logger.i(
                            "Recorder results: ${recorderResults.map { it.identifier }}"
                        )

                        assessmentResult.inputResults.addAll(recorderResults)

                        //TODO: Figure out better approach to determining filename for the results json file -nbrown 1/23/2023
                        val fileName = if (assessmentResult is MtbAssessmentResult) {
                            // Results from Northwestern built assessments are written to taskData.json
                            "taskData.json"
                        } else {
                            // Survey results are written to assessmentResult.json
                            "assessmentResult.json"
                        }
                        archiveUploader.archiveResultAndQueueUpload(
                            assessmentResult,
                            jsonCoder,
                            adherenceRecord.instanceGuid,
                            adherenceRecord.eventTimestamp,
                            startedTimeStamp,
                            fileName,
                            sessionExpire
                        )
                    } catch (e: CancellationException) {
                        Logger.e("Cancelled archiving", e)
                    }
                }

        } else {
            recorderRunner.cancel()
        }
    }

    override fun onCleared() {
        if (this::recorderRunner.isInitialized) {
            recorderRunner.cancel()
        }
        super.onCleared()
    }

}

open class MtbRootAssessmentViewModelFactory {

    open fun create(
        assessmentInfo: AssessmentPlaceholder,
        assessmentProvider: AssessmentRegistryProvider,
        nodeStateProvider: CustomNodeStateProvider?,
        assessmentResultCache: AssessmentResultCache?,
        assessmentInstanceId: String?,
        archiveUploader: AssessmentResultArchiveUploader,
        adherenceRecordRepo: AdherenceRecordRepo,
        adherenceRecord: AdherenceRecord,
        sessionExpiration: Instant,
        recorderRunnerFactory: RecorderRunner.RecorderRunnerFactory,
        studyId: String
    ): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(MtbRootAssessmentViewModel::class.java)) {

                    @Suppress("UNCHECKED_CAST")
                    return MtbRootAssessmentViewModel(
                        assessmentPlaceholder = assessmentInfo,
                        registryProvider = assessmentProvider,
                        nodeStateProvider = nodeStateProvider,
                        assessmentResultCache = assessmentResultCache,
                        assessmentInstanceId = assessmentInstanceId,
                        sessionExpiration = sessionExpiration,
                        archiveUploader = archiveUploader,
                        adherenceRecordRepo = adherenceRecordRepo,
                        adherenceRecord = adherenceRecord,
                        recorderRunnerFactory = recorderRunnerFactory,
                        studyId = studyId
                    ) as T
                }

                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}