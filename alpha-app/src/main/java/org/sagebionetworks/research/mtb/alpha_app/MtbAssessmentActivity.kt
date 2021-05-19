package org.sagebionetworks.research.mtb.alpha_app

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.datetime.Clock
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.koin.android.ext.android.inject
import org.sagebionetworks.assessmentmodel.AssessmentPlaceholder
import org.sagebionetworks.assessmentmodel.AssessmentRegistryProvider
import org.sagebionetworks.assessmentmodel.AssessmentResult
import org.sagebionetworks.assessmentmodel.JsonModuleInfo
import org.sagebionetworks.assessmentmodel.navigation.CustomNodeStateProvider
import org.sagebionetworks.assessmentmodel.navigation.FinishedReason
import org.sagebionetworks.assessmentmodel.navigation.NodeState
import org.sagebionetworks.assessmentmodel.presentation.AssessmentActivity
import org.sagebionetworks.assessmentmodel.presentation.RootAssessmentViewModel
import org.sagebionetworks.bridge.assessmentmodel.upload.AssessmentResultArchiveUploader
import org.sagebionetworks.bridge.kmm.shared.models.AdherenceRecord
import org.sagebionetworks.bridge.kmm.shared.repo.AdherenceRecordRepo

class MtbAssessmentActivity : AssessmentActivity() {

    val archiveUploader: AssessmentResultArchiveUploader by inject()
    val adherenceRecordRepo: AdherenceRecordRepo by inject()
    lateinit var adherenceRecord: AdherenceRecord

    override fun onCreate(savedInstanceState: Bundle?) {
        val adherenceRecordString = intent.getStringExtra(ARG_ADHERENCE_RECORD_KEY)!!
        val jsonCoder = Json {ignoreUnknownKeys = true }
        adherenceRecord = jsonCoder.decodeFromString(adherenceRecordString)
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
                    adherenceRecord
                )
        ).get(MtbRootAssessmentViewModel::class.java)

    companion object {
        const final val ARG_ADHERENCE_RECORD_KEY = "adherence_record_key"
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
    val adherenceRecord: AdherenceRecord
) : RootAssessmentViewModel(assessmentPlaceholder, registryProvider, nodeStateProvider) {

    val handleReadyToSave = MutableLiveData<String>()

    override fun handleReadyToSave(reason: FinishedReason, nodeState: NodeState) {
        if (reason.markFinished) {
            val finishedTimeStamp = nodeState.currentResult.endDateTime ?: Clock.System.now()
            val studyId = archiveUploader.authenticationRepository.currentStudyId()!!
            adherenceRecordRepo.createUpdateAdherenceRecord(adherenceRecord.copy(finishedOn = finishedTimeStamp), studyId)
        }
        if (reason.saveResult) {
            val moduleInfo =
                registryProvider.modules.first { it.hasAssessment(assessmentPlaceholder) }
            val jsonCoder = (moduleInfo as JsonModuleInfo).jsonCoder
            val assessmentResult = nodeState.currentResult as AssessmentResult

            // TODO: move to coroutine - liujoshua 04/09/2021
            archiveUploader.archiveResultAndQueueUpload(assessmentResult, jsonCoder)
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
        adherenceRecord: AdherenceRecord
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
                        adherenceRecord
                    ) as T
                }

                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}
