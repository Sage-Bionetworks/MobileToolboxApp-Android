package org.sagebionetworks.research.mtb.alpha_app

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.serialization.encodeToString
import org.koin.android.ext.android.inject
import org.koin.java.KoinJavaComponent.inject
import org.sagebionetworks.assessmentmodel.AssessmentPlaceholder
import org.sagebionetworks.assessmentmodel.AssessmentRegistryProvider
import org.sagebionetworks.assessmentmodel.AssessmentResult
import org.sagebionetworks.assessmentmodel.JsonModuleInfo
import org.sagebionetworks.assessmentmodel.navigation.CustomNodeStateProvider
import org.sagebionetworks.assessmentmodel.navigation.FinishedReason
import org.sagebionetworks.assessmentmodel.navigation.NodeState
import org.sagebionetworks.assessmentmodel.presentation.AssessmentActivity
import org.sagebionetworks.assessmentmodel.presentation.RootAssessmentViewModel
import org.sagebionetworks.bridge.kmm.shared.upload.UploadRequester
import org.sagebionetworks.bridge.assessmentmodel.upload.AssessmentResultArchiveUploader

class MtbAssessmentActivity : AssessmentActivity() {

    val uploadRequester: UploadRequester by inject()

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
                    uploadRequester
                )
        ).get(MtbRootAssessmentViewModel::class.java)

}

//This eventually can probably move down into BridgeClientKMM as a generalized BridgeAssessmentViewModel.
// Putting it here for now until we work out the details of what it needs. -nbrown 03/12/20201
class MtbRootAssessmentViewModel(
    assessmentPlaceholder: AssessmentPlaceholder,
    registryProvider: AssessmentRegistryProvider,
    nodeStateProvider: CustomNodeStateProvider?,
    val uploadRequester: UploadRequester
) : RootAssessmentViewModel(assessmentPlaceholder, registryProvider, nodeStateProvider) {

    val handleReadyToSave = MutableLiveData<String>()
    val context by inject(Context::class.java)
    val archiveUploader by inject(AssessmentResultArchiveUploader::class.java)

    override fun handleReadyToSave(reason: FinishedReason, nodeState: NodeState) {
        val moduleInfo = registryProvider.modules.first { it.hasAssessment(assessmentPlaceholder) }
        val jsonCoder = (moduleInfo as JsonModuleInfo).jsonCoder
        val assessmentResult = nodeState.currentResult as AssessmentResult

        archiveUploader.archiveResultAndQueueUpload(assessmentResult, jsonCoder)
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
        uploadRequester: UploadRequester
    ): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(MtbRootAssessmentViewModel::class.java)) {

                    @Suppress("UNCHECKED_CAST")
                    return MtbRootAssessmentViewModel(
                        assessmentInfo,
                        assessmentProvider,
                        nodeStateProvider,
                        uploadRequester
                    ) as T
                }

                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}
