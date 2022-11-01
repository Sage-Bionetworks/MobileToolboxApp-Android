package org.sagebionetworks.research.mobiletoolbox.app.notif

import android.Manifest
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
import kotlinx.datetime.DateTimePeriod
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.sagebionetworks.bridge.kmm.shared.repo.AuthenticationRepository
import org.sagebionetworks.bridge.kmm.shared.repo.ScheduledNotification
import org.sagebionetworks.research.mobiletoolbox.app.MtbMainActivity
import org.sagebionetworks.research.mobiletoolbox.app.R
import java.time.LocalDateTime
import java.time.ZoneId


class AlarmReceiver : BroadcastReceiver(), KoinComponent{

    private val authRepo: AuthenticationRepository by inject()

    override fun onReceive(context: Context, intent: Intent) {
        val studyId = authRepo.session()?.studyIds?.get(0)
        if (studyId == null) return // User is no longer logged in, so we don't have a session

        val notifJson = intent.getStringExtra(KEY_NOTIFICATION_JSON)
        notifJson?.let {
            //We have a notification to show
            val jsonCoder = Json {ignoreUnknownKeys = true }
            val notification: ScheduledNotification = jsonCoder.decodeFromString(notifJson)

            //Ideally we determine if the notification needs to be shown based on the completion status of the Session.
            // Currently there isn't a performant way given a Session instance id to get it's completion status.
            // We are recomputing the notifications everytime the today screen gets new data,
            // so we should only be showing notifications that still need to be completed. -nbrown 9/13/2001

            showNotification(context, notification)
            notification.repeatInterval?.let {
                //If this is a repeating notification schedule next one
                scheduleNotificationAlarm(context, notification, intent.getIntExtra(KEY_REQUEST_CODE, 0))
            }
        }
    }

    private fun showNotification(context: Context, notification: ScheduledNotification) {
        createNotificationChannel(context)
        val activityIntent = Intent(context, MtbMainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, activityIntent, PendingIntent.FLAG_IMMUTABLE)

        val notificationId = notification.instanceGuid.hashCode()
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setContentTitle(notification.message?.subject)
            .setContentText(notification.message?.message)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(context)) {
            if (areNotificationsEnabled()) {
                // notificationId is a unique int for each notification that you must define
                notify(notificationId, builder.build())
            }
        }
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
        const val KEY_NOTIFICATION_JSON = "NotificationJson"
        const val KEY_REQUEST_CODE = "RequestCode"

        fun clearNotification(context: Context, alarmManager: AlarmManager, requestCode: Int) {
            val alarmIntent = Intent(context, AlarmReceiver::class.java).let { intent ->
                intent.putExtra(KEY_REQUEST_CODE, requestCode)
                PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE)
            }
            alarmManager.cancel(alarmIntent)
        }

        fun scheduleNotificationAlarm(context: Context, notification: ScheduledNotification, requestCode: Int) {
            notification.nextScheduledTime()?.let { scheduledTime ->
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val alarmTimeInstant = scheduledTime.atZone(ZoneId.systemDefault()).toInstant()
                val alarmIntent = Intent(context, AlarmReceiver::class.java).let { intent ->
                    intent.putExtra(KEY_NOTIFICATION_JSON, Json.encodeToString(notification))
                    intent.putExtra(KEY_REQUEST_CODE, requestCode)
                    PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_IMMUTABLE)
                }
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        alarmTimeInstant.toEpochMilli(),
                        alarmIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        alarmTimeInstant.toEpochMilli(),
                        alarmIntent
                    )
                }
            }
        }
    }

}

/**
 * Extension function to go from a kotlinx.date.DateTimePeriod
 * to a java.time.Duration. Seems like something the kotlinx datetime library
 * should have, but as of 9/10/2001 it doesn't.
 */
fun DateTimePeriod.toDuration(): java.time.Duration {
    return java.time.Duration.parse(toString())
}

/**
 * Extension function to get the next time this notification should be shown.
 */
fun ScheduledNotification.nextScheduledTime(): LocalDateTime? {
    if (repeatInterval != null) {
        val now = LocalDateTime.now()
        var scheduledTime = scheduleOn.toJavaLocalDateTime()
        while (scheduledTime.isBefore(now)) {
            val duration = repeatInterval!!.toDuration()
            scheduledTime = scheduledTime.plus(duration)
        }
        if (scheduledTime.isBefore(repeatUntil!!.toJavaLocalDateTime())) {
            return scheduledTime
        }
        return null
    }
    return scheduleOn.toJavaLocalDateTime()
}
