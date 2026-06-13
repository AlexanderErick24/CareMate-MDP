package com.mdp.caremate.data.sources.local

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MedicationBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val firebaseSource = com.mdp.caremate.data.sources.remote.FirebaseSource()
                val userResult = firebaseSource.getCurrentUser()
                val user = userResult.getOrNull()
                if (user != null) {
                    val targetUid = if (user.role == "caregiver" && user.connectedPatientUid.isNotEmpty()) {
                        user.connectedPatientUid
                    } else {
                        user.uid
                    }
                    
                    val medRepo = com.mdp.caremate.data.repositories.MedRepositoryImpl()
                    val medicationsFlow = medRepo.observeTodaysMedications(targetUid)
                    val scheduler = MedicationAlarmScheduler(context)
                    
                    // get first snapshot
                    val medications = medicationsFlow.first()
                    medications.forEach { scheduler.schedule(it) }
                }
            } catch (e: Exception) {
                // ignore
            } finally {
                pendingResult.finish()
            }
        }
    }
}
