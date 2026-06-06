package com.mdp.caremate.data.sources.local

import android.app.PendingIntent
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.media.AudioAttributes
import android.net.Uri
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.mdp.caremate.MainActivity
import com.mdp.caremate.R
import com.mdp.caremate.data.model.Medication

class MedicationNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        ensureChannel(context)
        MedicationAlarmSoundPlayer.play(context)

        val medicationName = intent.getStringExtra(MedicationAlarmScheduler.EXTRA_MEDICATION_NAME)
            ?: context.getString(R.string.app_name)
        val medicationDosage = intent.getStringExtra(MedicationAlarmScheduler.EXTRA_MEDICATION_DOSAGE)
            ?: ""
        val medicationId = intent.getLongExtra(MedicationAlarmScheduler.EXTRA_MEDICATION_ID, 0L)
        val medicationHour = intent.getIntExtra(MedicationAlarmScheduler.EXTRA_MEDICATION_HOUR, -1)
        val medicationMinute = intent.getIntExtra(MedicationAlarmScheduler.EXTRA_MEDICATION_MINUTE, -1)

        val openAppIntent = Intent(context, MainActivity::class.java)
        val contentIntent = androidx.core.app.TaskStackBuilder.create(context)
            .addNextIntentWithParentStack(openAppIntent)
            .getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.med_notification_title))
            .setContentText(context.getString(R.string.med_notification_text, medicationName, medicationDosage))
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(context.getString(R.string.med_notification_detail))
            )
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVibrate(longArrayOf(0, 500, 250, 500))
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()

        NotificationManagerCompat.from(context).notify(
            medicationId.toInt(),
            notification
        )

        if (medicationId > 0 && medicationHour in 0..23 && medicationMinute in 0..59) {
            MedicationAlarmScheduler(context).schedule(
                Medication(
                    id = medicationId,
                    name = medicationName,
                    dosage = medicationDosage,
                    intakeHour = medicationHour,
                    intakeMinute = medicationMinute
                )
            )
        }
    }

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val alarmSoundUri: Uri = Settings.System.DEFAULT_ALARM_ALERT_URI
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.med_notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = context.getString(R.string.med_notification_channel_description)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 250, 500)
                setSound(
                    alarmSoundUri,
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "medication_alarm_channel_v2"
    }
}
