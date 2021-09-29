package org.sagebionetworks.research.mobiletoolbox.app.recorder

import android.content.Context
import android.util.Log
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.job
import org.sagebionetworks.assessmentmodel.passivedata.ResultData
import org.sagebionetworks.assessmentmodel.passivedata.asyncaction.AsyncActionConfiguration
import org.sagebionetworks.assessmentmodel.passivedata.recorder.Recorder
import org.sagebionetworks.assessmentmodel.passivedata.recorder.weather.AndroidWeatherRecorder
import org.sagebionetworks.assessmentmodel.passivedata.recorder.weather.WeatherConfiguration
import org.sagebionetworks.assessmentmodel.passivedata.recorder.weather.WeatherServiceConfiguration
import org.sagebionetworks.assessmentmodel.passivedata.recorder.weather.WeatherServiceProviderName
import org.sagebionetworks.research.mobiletoolbox.app.recorder.model.RecorderScheduledAssessmentConfig
import org.sagebionetworks.research.mobiletoolbox.app.recorder.model.rest.BackgroundRecordersConfigurationElement

/**
 * Recorder controller for Mobile Toolbox. Recorders starts with task launch and ends when the task
 * finishes.
 *
 * Does not implement the full feature set of AsyncActions and Recorders defined by AssessmentModel.
 */
class RecorderRunner(
    val context: Context,
    val httpClient: HttpClient,
    configs: List<RecorderScheduledAssessmentConfig>
) {
    private val tag = "RecorderRunner"

    private val scope = CoroutineScope(SupervisorJob())
    private lateinit var deferredRecorderResult: Deferred<List<ResultData>>

    private lateinit var recorders: List<Recorder<ResultData>>

    init {
        this.recorders = configs
            .mapNotNull { recorderFactory(it) }


        deferredRecorderResult =
            scope.async {
                Napier.d("Working in thread ${Thread.currentThread().name}, job ${coroutineContext[Job]}")
                Napier.i("Setting up all Recorder Deferreds")

                val results =
                    recorders
                        .mapNotNull { recorder ->
                            Log.i(
                                tag,
                                "Awaiting result for recorder: ${recorder.configuration.identifier}"
                            )
                            val deferredResult = recorder.result

                            deferredResult.invokeOnCompletion { throwable ->
                                if (throwable == null) {
                                    Log.i(
                                        tag,
                                        "Deferred completed for recorder: ${recorder.configuration.identifier}"
                                    )
                                } else {
                                    Log.e(
                                        tag,
                                        "Deferred threw exception for recorder: ${recorder.configuration.identifier}",
                                        throwable
                                    )
                                }
                            }
                            val result = deferredResult.await()
                            Log.i(
                                tag,
                                "Finished awaiting result for recorder: ${recorder.configuration.identifier}"
                            )
                            return@mapNotNull result
                        }
                Napier.d("Awaited results: $results")
                return@async results

            }
    }

    fun start() {
        Napier.i("Start called")
        scope.coroutineContext.job.start()
        recorders.forEach {
            val recorderId = it.configuration.identifier
            Napier.i("Starting recorder: $recorderId")
            try {
                it.start()
            } catch (e: Exception) {
                Napier.w("Error starting recorder: $recorderId", e)
            }
        }
        Napier.i("Start finished")
    }

    fun stop(): Deferred<List<ResultData>> {
        Napier.i("Stop called")

        recorders.forEach {
            val recorderId = it.configuration.identifier
            Napier.i("Stopping recorder: $recorderId")
            try {
                it.stop()
            } catch (e: Exception) {
                Napier.w("Error stopping recorder: $recorderId", e)
            }
        }

        return deferredRecorderResult
    }

    fun cancel() {
        recorders.forEach {
            val recorderId = it.configuration.identifier
            Napier.i("Cancelling recorder: $recorderId")
            try {
                it.cancel()
            } catch (e: Exception) {
                Napier.w("Error cancelling recorder: $recorderId", e)
            }
        }
    }

    internal fun recorderFactory(recorderScheduledAssessmentConfig: RecorderScheduledAssessmentConfig): Recorder<ResultData>? {
        val recorderConfig = recorderConfigFactory(recorderScheduledAssessmentConfig.recorder)
            ?: return null

        return when (recorderConfig.identifier) {

            "weather" -> {
                val weatherConfig = recorderConfig as WeatherConfiguration
                AndroidWeatherRecorder(
                    WeatherConfiguration(
                        "weather",
                        "weather",
                        weatherConfig.services
                    ),
                    httpClient,
                    context
                )
            }
            else -> {
                Napier.w("Unable to construct recorder ${recorderConfig.identifier}")
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

                if (services != null && services.isNotEmpty()) {
                    with(recorderConfig) {
                        WeatherConfiguration(
                            identifier,
                            type,
                            services
                        )
                    }
                } else {
                    return null
                }
            }
            else -> {
                Napier.w("Unable to construct recorder config ${recorderConfig.identifier}")
                null
            }
        }
    }

    class RecorderRunnerFactory(
        val context: Context,
        val httpClient: HttpClient
    ) {
        fun create(configs: List<RecorderScheduledAssessmentConfig>): RecorderRunner {
            return RecorderRunner(context, httpClient, configs)
        }
    }
}