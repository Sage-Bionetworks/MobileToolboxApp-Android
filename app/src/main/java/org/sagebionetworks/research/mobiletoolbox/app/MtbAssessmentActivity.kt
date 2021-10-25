package org.sagebionetworks.research.mobiletoolbox.app

import android.Manifest
import android.os.Bundle
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import edu.northwestern.mobiletoolbox.common.utils.AssessmentUtils
import io.github.aakira.napier.Napier
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.decodeFromString
import org.koin.android.ext.android.inject
import org.sagebionetworks.assessmentmodel.AssessmentPlaceholder
import org.sagebionetworks.assessmentmodel.AssessmentRegistryProvider
import org.sagebionetworks.assessmentmodel.navigation.CustomNodeStateProvider
import org.sagebionetworks.assessmentmodel.presentation.AssessmentActivity
import org.sagebionetworks.bridge.assessmentmodel.upload.AssessmentResultArchiveUploader
import org.sagebionetworks.bridge.kmm.shared.models.AdherenceRecord
import org.sagebionetworks.bridge.kmm.shared.repo.AdherenceRecordRepo
import org.sagebionetworks.research.mobiletoolbox.app.recorder.RecorderRunner
import org.sagebionetworks.research.mobiletoolbox.app.recorder.model.RecorderScheduledAssessmentConfig
import org.sagebionetworks.research.mobiletoolbox.app.recorder.model.recorderConfigJsonCoder

class MtbAssessmentActivity : AssessmentActivity() {

    val archiveUploader: AssessmentResultArchiveUploader by inject()
    val adherenceRecordRepo: AdherenceRecordRepo by inject()
    val recorderRunnerFactory: RecorderRunner.RecorderRunnerFactory by inject()
    lateinit var adherenceRecord: AdherenceRecord
    lateinit var sessionExpiration: Instant
    lateinit var recorderScheduledAssessmentConfigs: List<RecorderScheduledAssessmentConfig>

    lateinit var permissionResultCallback: ActivityResultCallback<Nothing>

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

        val recorderScheduledAssessmentConfigs: List<RecorderScheduledAssessmentConfig> =
            intent.getStringExtra(ARG_RECORDER_CONFIG_KEY)
                ?.let { recorderConfigJsonCoder.decodeFromString(it) } ?: listOf()

        recorderRunnerFactory.withConfig(recorderScheduledAssessmentConfigs)

        // TODO: add permission screens - liujoshua 2021-10-01
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
            Napier.i("RequestMultiplePermissions returned")
            (viewModel as MtbRootAssessmentViewModel).startRecorderRunner()
        }
        requestPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        )

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
                    sessionExpiration,
                    recorderRunnerFactory
                )
        ).get(MtbRootAssessmentViewModel::class.java)

    companion object {
        const final val ARG_ADHERENCE_RECORD_KEY = "adherence_record_key"
        const final val ARG_SESSION_EXPIRATION_KEY = "session_expiration_key"
        const final val ARG_RECORDER_CONFIG_KEY = "recorder_config_key"
    }

}

