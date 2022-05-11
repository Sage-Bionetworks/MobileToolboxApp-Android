package org.sagebionetworks.research.mobiletoolbox.app.recorder

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.job
import kotlinx.coroutines.supervisorScope
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.plus
import org.sagebionetworks.assessmentmodel.passivedata.ResultData
import org.sagebionetworks.assessmentmodel.passivedata.asyncaction.AsyncActionConfiguration
import org.sagebionetworks.assessmentmodel.passivedata.recorder.Recorder
import org.sagebionetworks.assessmentmodel.passivedata.recorder.audio.AudioRecorder
import org.sagebionetworks.assessmentmodel.passivedata.recorder.audio.AudioRecorderConfiguration
import org.sagebionetworks.assessmentmodel.passivedata.recorder.audio.createAudioLevelFlow
import org.sagebionetworks.assessmentmodel.passivedata.recorder.motion.DeviceMotionJsonFileResultRecorder
import org.sagebionetworks.assessmentmodel.passivedata.recorder.motion.MotionRecorderConfiguration
import org.sagebionetworks.assessmentmodel.passivedata.recorder.motion.createMotionRecorder
import org.sagebionetworks.assessmentmodel.passivedata.recorder.sensor.SensorEventComposite
import org.sagebionetworks.assessmentmodel.passivedata.recorder.weather.AndroidWeatherRecorder
import org.sagebionetworks.assessmentmodel.passivedata.recorder.weather.WeatherConfiguration
import org.sagebionetworks.assessmentmodel.passivedata.recorder.weather.WeatherServiceConfiguration
import org.sagebionetworks.research.mobiletoolbox.app.recorder.model.RecorderScheduledAssessmentConfig
import org.sagebionetworks.research.mobiletoolbox.app.recorder.model.rest.BackgroundRecordersConfigurationElement
import org.sagebionetworks.assessmentmodel.passivedata.recorder.sensor.sensorRecordModule
import org.sagebionetworks.research.mobiletoolbox.app.ui.login.PermissionPageType

/**
 * Recorder controller for Mobile Toolbox. Recorders starts with task launch and ends when the task
 * finishes.
 *
 * Does not implement the full feature set of AsyncActions and Recorders defined by AssessmentModel.
 */
class RecorderRunner(
    val context: Context,
    val httpClient: HttpClient,
    configs: List<RecorderScheduledAssessmentConfig>,
    val taskIdentifier: String
) {
    private val tag = "RecorderRunner"

    private val scope = CoroutineScope(Dispatchers.IO)
    private val deferredRecorderResult: Deferred<List<ResultData>>
    private val recorders: List<Recorder<out ResultData>>

    init {
        this.recorders = configs
            .filterNot { recorderScheduledAssessmentConfig ->
                val isDisabled =
                    recorderScheduledAssessmentConfig.isRecorderDisabled(taskIdentifier)
                if (isDisabled) {
                    Logger.i("Skipping ${recorderScheduledAssessmentConfig.recorder.identifier} disabled for this task")
                }
                isDisabled
            }
            .mapNotNull { recorderFactory(it) }


        deferredRecorderResult =
            scope.async {
                Logger.d("Working in thread ${Thread.currentThread().name}, job ${coroutineContext[Job]}")
                supervisorScope {

                    val results =
                        recorders
                            .mapNotNull { recorder ->
                                Logger.i(
                                    "Awaiting result for recorder: ${recorder.configuration.identifier}"
                                )
                                val deferredResult = recorder.result

                                deferredResult.invokeOnCompletion { throwable ->
                                    if (throwable == null) {
                                        Logger.d(
                                            "Deferred completed for recorder: ${recorder.configuration.identifier}"
                                        )
                                    } else if (throwable is CancellationException) {
                                        Logger.d(
                                            "Deferred cancelled for recorder: ${recorder.configuration.identifier}",
                                            throwable
                                        )
                                    } else {
                                        Logger.w(
                                            "Deferred threw unhandled exception for recorder: ${recorder.configuration.identifier}",
                                            throwable
                                        )
                                    }
                                }
                                return@mapNotNull try {
                                    val result = deferredResult.await()
                                    Logger.i(
                                        "Finished awaiting result for recorder: ${recorder.configuration.identifier}"
                                    )

                                    result
                                } catch (e: Throwable) {
                                    Logger.w(
                                        "Error waiting for deferred recorder result for recorder: ${recorder.configuration.identifier}",
                                        e
                                    )
                                    null
                                }
                            }


                    Logger.d("Awaited results: $results")
                    return@supervisorScope results
                }
            }
    }

    fun start() {
        Logger.i("Start called")
        scope.coroutineContext.job.start()
        recorders.forEach {
            val recorderId = it.configuration.identifier
            Logger.i("Starting recorder: $recorderId")
            try {
                it.start()
            } catch (e: Exception) {
                Logger.w("Error starting recorder: $recorderId", e)
            }
        }
        Logger.i("Start finished")
    }

    fun stop(): Deferred<List<ResultData>> {
        Logger.i("Stop called")

        recorders.forEach {
            val recorderId = it.configuration.identifier
            Logger.i("Stopping recorder: $recorderId")
            try {
                it.stop()
            } catch (e: Exception) {
                Logger.w("Error stopping recorder: $recorderId", e)
            }
        }

        return deferredRecorderResult
    }

    fun cancel() {
        recorders.forEach {
            val recorderId = it.configuration.identifier
            Logger.i("Cancelling recorder: $recorderId")
            try {
                it.cancel()
            } catch (e: Exception) {
                Logger.w("Error cancelling recorder: $recorderId", e)
            }
        }
    }

    internal fun recorderFactory(recorderScheduledAssessmentConfig: RecorderScheduledAssessmentConfig): Recorder<out ResultData>? {
        val recorderConfig = recorderConfigFactory(recorderScheduledAssessmentConfig.recorder)
            ?: return null

        return when (recorderConfig) {

            is WeatherConfiguration -> {
                AndroidWeatherRecorder(
                    WeatherConfiguration(
                        recorderConfig.identifier,
                        null,
                        recorderConfig.services
                    ),
                    httpClient,
                    context
                )
            }
            is MotionRecorderConfiguration -> {
                with(recorderConfig) {
                    //TODO joliu real values
                    DeviceMotionJsonFileResultRecorder(
                        identifier,
                        recorderConfig,
                        CoroutineScope(Dispatchers.IO),
                        createMotionRecorder(context).getSensorData()
                            .mapNotNull {
                                return@mapNotNull if (it is SensorEventComposite.SensorChanged) {
                                    it.sensorEvent
                                } else {
                                    null
                                }
                            },
                        context,
                        Json {
                            serializersModule += sensorRecordModule
                        }
                    )
                }

            }
            is AudioRecorderConfiguration -> {
                with(recorderConfig) {
                    AudioRecorder(
                        identifier = identifier,
                        configuration = recorderConfig,
                        scope = CoroutineScope(Dispatchers.IO),
                        flow = recorderConfig.createAudioLevelFlow(context),
                        context = context
                    )
                }
            }
            else -> {
                Logger.w("Unable to construct recorder ${recorderConfig.identifier}")
                null
            }
        }
    }

    internal fun recorderConfigFactory(recorderConfig: BackgroundRecordersConfigurationElement.Recorder): AsyncActionConfiguration? {

        return when (recorderConfig.type) {
            WeatherConfiguration.TYPE -> {
                val services = recorderConfig.services?.map { service ->
                    return@map with(service) {
                        WeatherServiceConfiguration(
                            identifier,
                            provider,
                            key
                        )
                    }
                }

                if (services?.isNotEmpty() == true) {
                    with(recorderConfig) {
                        WeatherConfiguration(
                            identifier,
                            null,
                            services
                        )
                    }
                } else {
                    return null
                }
            }
            MotionRecorderConfiguration.TYPE -> {
                // Check if user has given permission to use motion sensor
                if (PermissionPageType.MOTION_PAGE.getAllowToggle(context) == true) {
                    with(recorderConfig) {
                        MotionRecorderConfiguration(
                            identifier = identifier,
                            requiresBackgroundAudio = false,
                            shouldDeletePrevious = false
                        )
                    }
                } else {
                    return null
                }
            }
            AudioRecorderConfiguration.TYPE -> {
                //Check if we have permission to record audio
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED) {
                    with(recorderConfig) {
                        AudioRecorderConfiguration(
                            identifier = identifier
                        )
                    }
                } else {
                    return null
                }
            }
            else -> {
                Logger.w("Unable to construct recorder config ${recorderConfig.identifier}")
                null
            }
        }
    }

    class RecorderRunnerFactory(
        val context: Context,
        val httpClient: HttpClient
    ) {
        lateinit var configs: List<RecorderScheduledAssessmentConfig>
        fun withConfig(configs: List<RecorderScheduledAssessmentConfig>) {
            this.configs = configs
        }

        fun create(
            taskIdentifier: String
        ): RecorderRunner {
            return RecorderRunner(context, httpClient, configs, taskIdentifier)
        }
    }
}