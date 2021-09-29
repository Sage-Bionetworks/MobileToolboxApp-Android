package org.sagebionetworks.research.mobiletoolbox.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.sagebionetworks.assessmentmodel.AssessmentPlaceholder
import org.sagebionetworks.assessmentmodel.AssessmentRegistryProvider
import org.sagebionetworks.assessmentmodel.AssessmentResult
import org.sagebionetworks.assessmentmodel.JsonModuleInfo
import org.sagebionetworks.assessmentmodel.navigation.CustomNodeStateProvider
import org.sagebionetworks.assessmentmodel.navigation.FinishedReason
import org.sagebionetworks.assessmentmodel.navigation.NodeState
import org.sagebionetworks.assessmentmodel.navigation.SaveResults
import org.sagebionetworks.assessmentmodel.passivedata.recorder.coroutineExceptionLogger
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
    val archiveUploader: AssessmentResultArchiveUploader,
    val adherenceRecordRepo: AdherenceRecordRepo,
    val adherenceRecord: AdherenceRecord,
    val sessionExpiration: Instant,
    val recorderRunner: RecorderRunner
) : RootAssessmentViewModel(assessmentPlaceholder, registryProvider, nodeStateProvider) {

    init {
        recorderRunner.start()
    }

    override fun handleReadyToSave(reason: FinishedReason, nodeState: NodeState) {
        Napier.d("FinishedReason: ${reason.javaClass.simpleName}")
        val finishedTimeStamp = if (reason.markFinished) {
            nodeState.currentResult.endDateTime ?: Clock.System.now()
        } else {
            null
        }
        val startedTimeStamp = nodeState.currentResult.startDateTime
        val studyId = archiveUploader.authenticationRepository.currentStudyId()!!
        adherenceRecordRepo.createUpdateAdherenceRecord(
            adherenceRecord.copy(
                startedOn = startedTimeStamp,
                finishedOn = finishedTimeStamp, declined = reason.declined
            ), studyId
        )
if(true){
//        if (reason.saveResult == SaveResults.Now || reason.saveResult == SaveResults.WhenSessionExpires) {
            val moduleInfo =
                registryProvider.modules.first { it.hasAssessment(assessmentPlaceholder) }
            val jsonCoder = (moduleInfo as JsonModuleInfo).jsonCoder
            val assessmentResult = nodeState.currentResult as AssessmentResult

            val sessionExpire: Instant? = if (reason.saveResult == SaveResults.WhenSessionExpires) {
                sessionExpiration
            } else {
                null
            }

            CoroutineScope(Dispatchers.IO)
                .launch(coroutineExceptionLogger) {
                    Napier.d("Working in thread ${Thread.currentThread().name}, job ${coroutineContext[Job]}")

                    try {

                        Napier.i("Recorders stop and await")
                        val recorderResultsDeferred = recorderRunner.stop()

                        Napier.i("Recorders stop done")

                        val recorderResults = recorderResultsDeferred.await()

                        Napier.i(
                            "Recorder results: ${recorderResults.map { "it.identifier," }}"
                        )
                        (archiveUploader as MtbAssessmentResultArchiveUploader)
                            .asyncResults.addAll(
                                recorderResults
                            )
                        archiveUploader.archiveResultAndQueueUpload(
                            assessmentResult,
                            jsonCoder,
                            adherenceRecord.instanceGuid,
                            adherenceRecord.eventTimestamp,
                            sessionExpire
                        )
                    } catch (e: CancellationException) {
                        Napier.w("Cancelled archiving", e)
                    }
                }

        } else {
            recorderRunner.cancel()
        }
    }
}

open class MtbRootAssessmentViewModelFactory() {

    open fun create(
        assessmentInfo: AssessmentPlaceholder,
        assessmentProvider: AssessmentRegistryProvider,
        nodeStateProvider: CustomNodeStateProvider?,
        archiveUploader: AssessmentResultArchiveUploader,
        adherenceRecordRepo: AdherenceRecordRepo,
        adherenceRecord: AdherenceRecord,
        sessionExpiration: Instant,
        recorderRunner: RecorderRunner
    ): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(MtbRootAssessmentViewModel::class.java)) {

                    @Suppress("UNCHECKED_CAST")
                    return MtbRootAssessmentViewModel(
                        assessmentInfo,
                        assessmentProvider,
                        nodeStateProvider,
                        archiveUploader,
                        adherenceRecordRepo,
                        adherenceRecord,
                        sessionExpiration,
                        recorderRunner
                    ) as T
                }

                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}