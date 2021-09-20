package org.sagebionetworks.research.mobiletoolbox.app.notif

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.firstOrNull
import org.sagebionetworks.bridge.kmm.shared.cache.ResourceResult
import org.sagebionetworks.bridge.kmm.shared.repo.AuthenticationRepository
import org.sagebionetworks.bridge.kmm.shared.repo.ScheduleTimelineRepo
import java.util.concurrent.TimeUnit

class ScheduleNotificationsWorker(appContext: Context, workerParams: WorkerParameters,
                                  private val authRepo: AuthenticationRepository,
                                  private val timelineRepo: ScheduleTimelineRepo
):
    CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        var result = Result.success()
        // If we don't have a study id the user is no longer logged in, so we don't have a session
        val studyId = authRepo.session()?.studyIds?.get(0) ?: return result

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
        val numToSchedule = 100
        //First schedule/updated notification alarms
        var requestCode = 0
        notificationsList?.take(numToSchedule)?.forEach { notification ->
            //Each alarm has a unique request code so they don't clobber each other
            AlarmReceiver.scheduleNotificationAlarm(applicationContext, notification, requestCode)
            requestCode++
        }
        //Second cancel any old alarms that weren't updated
        val alarmManager =
            applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        for (i in requestCode until numToSchedule) {
            AlarmReceiver.clearNotification(applicationContext, alarmManager, i)
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