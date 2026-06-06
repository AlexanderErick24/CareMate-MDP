package com.mdp.caremate.data.sources.local

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.mdp.caremate.data.model.toMedication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MedicationBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val medications = db.medicationDao().getEnabledMedications()
                val scheduler = MedicationAlarmScheduler(context)
                medications.map { it.toMedication() }.forEach { scheduler.schedule(it) }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
