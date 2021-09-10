package org.sagebionetworks.research.mobiletoolbox.app.notif

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import org.sagebionetworks.bridge.kmm.shared.cache.ResourceResult
import org.sagebionetworks.bridge.kmm.shared.repo.AuthenticationRepository
import org.sagebionetworks.bridge.kmm.shared.repo.ScheduleTimelineRepo
import java.util.concurrent.TimeUnit

class ScheduleNotificationsWorker(appContext: Context, workerParams: WorkerParameters,
                                  private val authRepo: AuthenticationRepository,
                                  private val timelineRepo: ScheduleTimelineRepo
):
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        var result = Result.success()
        val studyId = authRepo.session()?.studyIds?.get(0)
        if (studyId == null) return result // User is no longer logged in, so we don't have a session
        runBlocking {
            //Get sessions for today
            val sessionsListResource =
                timelineRepo.getSessionsForToday(studyId, includeAllNotifications = true)
                    .firstOrNull {
                        it is ResourceResult.Success
                    }
            if (sessionsListResource == null) {
                result = Result.retry()
            }
            val notificationsList =
                (sessionsListResource as? ResourceResult.Success)?.data?.notifications

            //Schedule notifications - Each app can have a max of 500 alarms scheduled
            // Worker should run everyday, so 100 is more than plenty -nbrown 9/9/2001
            notificationsList?.take(100)?.forEach { notification ->
                AlarmReceiver.scheduleNotificationAlarm(applicationContext, notification)
            }
        }

        // Indicate whether the work finished successfully with the Result
        return result
    }

    companion object {

        /**
         * Enqueue a ScheduleNotificationWorker to run once a day.
         */
        fun enqueueDailyScheduleNotificationWorker(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val workRequest =
                PeriodicWorkRequestBuilder<ScheduleNotificationsWorker>(1, TimeUnit.DAYS)
                    .setConstraints(constraints)
                    .addTag("ScheduleNotificationsWorker")
                    .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork("DailyScheduleNotificationsWorker", ExistingPeriodicWorkPolicy.KEEP, workRequest)
        }

        /**
         * Enqueue a ScheduleNotificationWorker to run now.
         * This is called after login.
         */
        fun runScheduleNotificationWorker(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            val workRequest =
                OneTimeWorkRequestBuilder<ScheduleNotificationsWorker>()
                    .setConstraints(constraints)
                    // Additional configuration
                    .build()

            WorkManager.getInstance(context).enqueueUniqueWork("OnetimeScheduleNotificationsWorker", ExistingWorkPolicy.REPLACE, workRequest)
        }
    }
}