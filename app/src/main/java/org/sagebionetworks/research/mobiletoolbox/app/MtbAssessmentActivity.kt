package org.sagebionetworks.research.mobiletoolbox.app

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import edu.northwestern.mobiletoolbox.common.utils.AssessmentUtils
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.decodeFromString
import org.koin.android.ext.android.inject
import org.sagebionetworks.assessmentmodel.AssessmentPlaceholder
import org.sagebionetworks.assessmentmodel.AssessmentRegistryProvider
import org.sagebionetworks.assessmentmodel.AssessmentResult
import org.sagebionetworks.assessmentmodel.JsonModuleInfo
import org.sagebionetworks.assessmentmodel.navigation.CustomNodeStateProvider
import org.sagebionetworks.assessmentmodel.navigation.FinishedReason
import org.sagebionetworks.assessmentmodel.navigation.NodeState
import org.sagebionetworks.assessmentmodel.navigation.SaveResults
import org.sagebionetworks.assessmentmodel.presentation.AssessmentActivity
import org.sagebionetworks.assessmentmodel.presentation.RootAssessmentViewModel
import org.sagebionetworks.bridge.assessmentmodel.upload.AssessmentResultArchiveUploader
import org.sagebionetworks.bridge.kmm.shared.models.AdherenceRecord
import org.sagebionetworks.bridge.kmm.shared.repo.AdherenceRecordRepo
import org.sagebionetworks.research.mobiletoolbox.app.recorder.model.RecorderScheduledAssessmentConfig
import org.sagebionetworks.research.mobiletoolbox.app.recorder.model.recorderConfigJsonCoder

class MtbAssessmentActivity : AssessmentActivity() {

    val archiveUploader: AssessmentResultArchiveUploader by inject()
    val adherenceRecordRepo: AdherenceRecordRepo by inject()
    lateinit var adherenceRecord: AdherenceRecord
    lateinit var sessionExpiration: Instant
    lateinit var recorderScheduledAssessmentConfig: List<RecorderScheduledAssessmentConfig>

    //Fix for June so that MTB assessments are full screen
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) AssessmentUtils.hideNavigationBar(window)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val adherenceRecordString = intent.getStringExtra(ARG_ADHERENCE_RECORD_KEY)!!
        adherenceRecord = recorderConfigJsonCoder.decodeFromString(adherenceRecordString)
        sessionExpiration = Instant.fromEpochMilliseconds(
            intent.getLongExtra(
                ARG_SESSION_EXPIRATION_KEY,
                Clock.System.now().toEpochMilliseconds()
            )
        )
        recorderScheduledAssessmentConfig = intent.getStringExtra(ARG_RECORDER_CONFIG_KEY)
            ?.let { recorderConfigJsonCoder.decodeFromString(it) } ?: listOf()

        // TODO: call recorders

        super.onCreate(savedInstanceState)
    }

    override fun initViewModel(
        assessmentInfo: AssessmentPlaceholder,
        assessmentProvider: AssessmentRegistryProvider,
        customNodeStateProvider: CustomNodeStateProvider?
    ) =
        ViewModelProvider(
            this, MtbRootAssessmentViewModelFactory()
                .create(
                    assessmentInfo,
                    assessmentProvider,
                    customNodeStateProvider,
                    archiveUploader,
                    adherenceRecordRepo,
                    adherenceRecord,
                    sessionExpiration
                )
        ).get(MtbRootAssessmentViewModel::class.java)

    companion object {
        const final val ARG_ADHERENCE_RECORD_KEY = "adherence_record_key"
        const final val ARG_SESSION_EXPIRATION_KEY = "session_expiration_key"
        const final val ARG_RECORDER_CONFIG_KEY = "recorder_config_key"
    }

}

//This eventually can probably move down into BridgeClientKMM as a generalized BridgeAssessmentViewModel.
// Putting it here for now until we work out the details of what it needs. -nbrown 03/12/20201
class MtbRootAssessmentViewModel(
    assessmentPlaceholder: AssessmentPlaceholder,
    registryProvider: AssessmentRegistryProvider,
    nodeStateProvider: CustomNodeStateProvider?,
    val archiveUploader: AssessmentResultArchiveUploader,
    val adherenceRecordRepo: AdherenceRecordRepo,
    val adherenceRecord: AdherenceRecord,
    val sessionExpiration: Instant
) : RootAssessmentViewModel(assessmentPlaceholder, registryProvider, nodeStateProvider) {

    val handleReadyToSave = MutableLiveData<String>()

    override fun handleReadyToSave(reason: FinishedReason, nodeState: NodeState) {
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

        if (reason.saveResult == SaveResults.Now || reason.saveResult == SaveResults.WhenSessionExpires) {
            val moduleInfo =
                registryProvider.modules.first { it.hasAssessment(assessmentPlaceholder) }
            val jsonCoder = (moduleInfo as JsonModuleInfo).jsonCoder
            val assessmentResult = nodeState.currentResult as AssessmentResult

            val sessionExpire: Instant? = if (reason.saveResult == SaveResults.WhenSessionExpires) {
                sessionExpiration
            } else {
                null
            }

            // TODO: move to coroutine - liujoshua 04/09/2021
            archiveUploader.archiveResultAndQueueUpload(
                assessmentResult,
                jsonCoder,
                adherenceRecord.instanceGuid,
                sessionExpire
            )
        }
    }

    override fun handleFinished(reason: FinishedReason, nodeState: NodeState) {
        //This should check that upload has been queued before making call to super (which will finish activity).
        super.handleFinished(reason, nodeState)
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
        sessionExpiration: Instant
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
                        sessionExpiration
                    ) as T
                }

                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}
