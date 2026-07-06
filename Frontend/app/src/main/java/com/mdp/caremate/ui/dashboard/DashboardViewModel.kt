package com.mdp.caremate.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.mdp.caremate.data.model.Medication
import com.mdp.caremate.data.repositories.MedRepository
import com.mdp.caremate.data.repositories.MedRepositoryImpl
import com.mdp.caremate.data.sources.local.MedicationAlarmScheduler
import com.mdp.caremate.data.sources.remote.FirebaseSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val medRepository: MedRepository = MedRepositoryImpl()
    private val firebaseSource = FirebaseSource()
    private val scheduler = MedicationAlarmScheduler(application)

    private val targetUidFlow = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            val userResult = firebaseSource.getCurrentUser()
            if (userResult.isSuccess) {
                val user = userResult.getOrNull()
                if (user != null) {
                    targetUidFlow.value = if (user.role == "caregiver" && user.connectedPatientUid.isNotEmpty()) {
                        user.connectedPatientUid
                    } else {
                        user.uid
                    }
                }
            }
        }
    }

    val todaysMedications: LiveData<List<Medication>> = targetUidFlow.flatMapLatest { uid ->
        if (uid != null) {
            medRepository.observeTodaysMedications(uid)
        } else {
            emptyFlow()
        }
    }.asLiveData()

    val historyList: LiveData<List<com.mdp.caremate.data.model.MedicationHistory>> = targetUidFlow.flatMapLatest { uid ->
        if (uid != null) {
            medRepository.observeHistory(uid)
        } else {
            emptyFlow()
        }
    }.asLiveData()

    fun updateMedicationTakenStatus(medication: Medication, isTakenToday: Boolean, photoUrl: String? = null) {
        viewModelScope.launch {
            try {
                val uid = targetUidFlow.value
                if (uid != null) {
                    medRepository.setMedicationTakenStatus(uid, medication.id, isTakenToday, photoUrl)
                    
                    // Trigger AI Verification if photo was uploaded
                    if (isTakenToday && !photoUrl.isNullOrEmpty()) {
                        try {
                            val app = getApplication<com.mdp.caremate.CareMateApplication>()
                            val premiumRepo = app.premiumRepository
                            val alert = premiumRepo.verifyMedication(photoUrl, medication.name)
                            
                            // Save to Firestore so family can see
                            val caregiverId = uid // the caregiver taking the action
                            val finalAlert = alert.copy(caregiverId = caregiverId)
                            premiumRepo.saveAiAlert(finalAlert)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            // Handle AI failure silently or log it
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteMedication(medication: Medication) {
        viewModelScope.launch {
            try {
                val uid = targetUidFlow.value
                if (uid != null) {
                    medRepository.deleteMedication(uid, medication)
                    scheduler.cancel(medication.id)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
