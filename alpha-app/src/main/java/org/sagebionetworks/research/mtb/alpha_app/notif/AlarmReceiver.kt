package org.sagebionetworks.research.mtb.alpha_app.notif

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.sagebionetworks.bridge.kmm.shared.cache.ResourceResult
import org.sagebionetworks.bridge.kmm.shared.repo.ActivityEventsRepo
import org.sagebionetworks.bridge.kmm.shared.repo.AuthenticationRepository
import org.sagebionetworks.bridge.kmm.shared.repo.ScheduleTimelineRepo
import org.sagebionetworks.research.mtb.alpha_app.MtbMainActivity
import org.sagebionetworks.research.mtb.alpha_app.R
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * For our June release this is configured to show a notification every day at noon,
 * so long as there activities scheduled that haven't been completed. This assumes that there
 * is one Session with a NotificationMessage configured for it.
 */
class AlarmReceiver : BroadcastReceiver(), KoinComponent{

    private val authRepo: AuthenticationRepository by inject()
    private val activityEventsRepo: ActivityEventsRepo by inject()
    private val timelineRepo: ScheduleTimelineRepo by inject()


    override fun onReceive(context: Context, intent: Intent) {
        val studyId = authRepo.session()?.studyIds?.get(0)
        if (studyId == null) return // User is no longer logged in, so we don't have a session
        runBlocking {
            //Get sessions for today
            val sessionsListResource = timelineRepo.getSessionsForToday(studyId).firstOrNull {
                it is ResourceResult.Success
            }
            val sessionsList = (sessionsListResource as? ResourceResult.Success)?.data
            if (sessionsList != null && sessionsList.isNotEmpty()) {
                val sessionWindow = sessionsList[0]
                if (sessionWindow.sessionInfo.notifications?.isNotEmpty() == true) {
                    val notificationMessage = sessionWindow.sessionInfo.notifications?.get(0)
                    createNotificationChannel(context)
                    val activityIntent = Intent(context, MtbMainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, activityIntent, 0)

                    val notificationId = 1
                    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification_icon)
                        .setContentTitle(notificationMessage?.message?.subject)
                        .setContentText(notificationMessage?.message?.message)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)

                    with(NotificationManagerCompat.from(context)) {
                        // notificationId is a unique int for each notification that you must define
                        notify(notificationId, builder.build())
                    }
                }
            }
        }
        scheduleReminderAlarm(context)
    }

    private fun createNotificationChannel(context: Context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Reminder Notifications"
            val descriptionText =  "Reminder notifications to complete activities"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "AssessmentReminderChannel"


        /**
         * Schedule and alarm to fire the next time it is NOON
         */
        fun scheduleReminderAlarm(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val alarmIntent = Intent(context, AlarmReceiver::class.java).let { intent ->
                PendingIntent.getBroadcast(context, 0, intent, 0)
            }

            var alarmTime = ZonedDateTime.of(LocalDate.now(), LocalTime.NOON, ZoneId.systemDefault())

            if (alarmTime.isBefore(ZonedDateTime.now())) {
                //Noon already happened today, set for tomorrow
                alarmTime = alarmTime.plusDays(1)
            }

            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmTime.toInstant().toEpochMilli(), alarmIntent)
        }
    }

}