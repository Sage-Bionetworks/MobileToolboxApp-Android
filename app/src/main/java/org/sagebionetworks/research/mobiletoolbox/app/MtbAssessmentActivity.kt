package org.sagebionetworks.research.mobiletoolbox.app

import android.Manifest
import android.os.Bundle
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import co.touchlab.kermit.Logger
import edu.northwestern.mobiletoolbox.common.utils.AssessmentUtils
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.decodeFromString
import org.koin.android.ext.android.inject
import org.sagebionetworks.assessmentmodel.AssessmentPlaceholder
import org.sagebionetworks.assessmentmodel.AssessmentRegistryProvider
import org.sagebionetworks.assessmentmodel.AssessmentResultCache
import org.sagebionetworks.assessmentmodel.navigation.CustomNodeStateProvider
import org.sagebionetworks.assessmentmodel.passivedata.recorder.audio.AudioRecorderConfiguration
import org.sagebionetworks.assessmentmodel.passivedata.recorder.weather.WeatherConfiguration
import org.sagebionetworks.assessmentmodel.presentation.AssessmentActivity
import org.sagebionetworks.bridge.assessmentmodel.upload.AssessmentResultArchiveUploader
import org.sagebionetworks.bridge.kmm.shared.models.AdherenceRecord
import org.sagebionetworks.bridge.kmm.shared.repo.AdherenceRecordRepo
import org.sagebionetworks.bridge.kmm.shared.repo.AuthenticationRepository
import org.sagebionetworks.research.mobiletoolbox.app.recorder.RecorderRunner
import org.sagebionetworks.research.mobiletoolbox.app.recorder.model.RecorderScheduledAssessmentConfig
import org.sagebionetworks.research.mobiletoolbox.app.recorder.model.recorderConfigJsonCoder

class MtbAssessmentActivity : AssessmentActivity() {

    private val archiveUploader: AssessmentResultArchiveUploader by inject()
    private val adherenceRecordRepo: AdherenceRecordRepo by inject()
    private val recorderRunnerFactory: RecorderRunner.RecorderRunnerFactory by inject()
    private val assessmentResultCache: AssessmentResultCache by inject()
    private val authRepo: AuthenticationRepository by inject()
    private lateinit var adherenceRecord: AdherenceRecord
    private lateinit var sessionExpiration: Instant
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

        super.onCreate(savedInstanceState)

        //super.onCreate needs to be called before here so that viewModel is initialized
        val permissionList = mutableListOf<String>()
        if (recorderScheduledAssessmentConfigs.any { it.recorder.type == WeatherConfiguration.TYPE && !it.isRecorderDisabled(viewModel.assessmentPlaceholder.identifier) }) {
            permissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION)
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (recorderScheduledAssessmentConfigs.any { it.recorder.type == AudioRecorderConfiguration.TYPE && !it.isRecorderDisabled(viewModel.assessmentPlaceholder.identifier) }) {
            permissionList.add(Manifest.permission.RECORD_AUDIO)
        }

        if (permissionList.isNotEmpty()) {
            val requestPermissionLauncher = registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) {
                Logger.i("RequestMultiplePermissions returned")
                (viewModel as MtbRootAssessmentViewModel).startRecorderRunner()
            }
            requestPermissionLauncher.launch(
                permissionList.toTypedArray()
            )
        } else {
            (viewModel as MtbRootAssessmentViewModel).startRecorderRunner()
        }

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
                    assessmentResultCache,
                    adherenceRecord.instanceGuid,
                    archiveUploader,
                    adherenceRecordRepo,
                    adherenceRecord,
                    sessionExpiration,
                    recorderRunnerFactory,
                    authRepo.currentStudyId()!!
                )
        ).get(MtbRootAssessmentViewModel::class.java)

    companion object {
        const val ARG_ADHERENCE_RECORD_KEY = "adherence_record_key"
        const val ARG_SESSION_EXPIRATION_KEY = "session_expiration_key"
        const val ARG_RECORDER_CONFIG_KEY = "recorder_config_key"
    }

}

