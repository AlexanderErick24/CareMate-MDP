package com.mdp.caremate.data.sources.local

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.mdp.caremate.data.model.Medication
import java.util.Calendar

class MedicationAlarmScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(medication: Medication) {
        cancel(medication.id)

        val triggerAtMillis = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, medication.intakeHour)
            set(Calendar.MINUTE, medication.intakeMinute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }.timeInMillis

        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            createPendingIntent(medication)
        )
    }

    fun cancel(medicationId: String) {
        alarmManager.cancel(createPendingIntent(medicationId))
    }

    private fun createPendingIntent(medication: Medication): PendingIntent {
        val intent = Intent(context, MedicationNotificationReceiver::class.java).apply {
            putExtra(EXTRA_MEDICATION_ID, medication.id)
            putExtra(EXTRA_MEDICATION_NAME, medication.name)
            putExtra(EXTRA_MEDICATION_DOSAGE, medication.dosage)
            putExtra(EXTRA_MEDICATION_HOUR, medication.intakeHour)
            putExtra(EXTRA_MEDICATION_MINUTE, medication.intakeMinute)
        }
        return PendingIntent.getBroadcast(
            context,
            medication.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createPendingIntent(medicationId: String): PendingIntent {
        val intent = Intent(context, MedicationNotificationReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            medicationId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        const val EXTRA_MEDICATION_ID = "extra_medication_id"
        const val EXTRA_MEDICATION_NAME = "extra_medication_name"
        const val EXTRA_MEDICATION_DOSAGE = "extra_medication_dosage"
        const val EXTRA_MEDICATION_HOUR = "extra_medication_hour"
        const val EXTRA_MEDICATION_MINUTE = "extra_medication_minute"
    }
}
