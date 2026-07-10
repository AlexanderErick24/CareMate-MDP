package com.mdp.caremate.ui.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
class DashboardViewModel @JvmOverloads constructor(
    application: Application,
    private val medRepository: MedRepository = MedRepositoryImpl(),
    private val firebaseSource: FirebaseSource = FirebaseSource(),
    private val scheduler: MedicationAlarmScheduler = MedicationAlarmScheduler(application)
) : AndroidViewModel(application) {

    private val targetUidFlow = MutableStateFlow<String?>(null)

    private val _currentUserFlow = MutableStateFlow<com.mdp.caremate.data.model.User?>(null)
    val currentUserFlow: LiveData<com.mdp.caremate.data.model.User?> = _currentUserFlow.asLiveData()

    private val _isLocalMode = MutableStateFlow(false)
    val isLocalMode: LiveData<Boolean> = _isLocalMode.asLiveData()

    private val _statusMessage = MutableLiveData<String?>()
    val statusMessage: LiveData<String?> = _statusMessage

    init {
        refreshUserData()
    }

    fun refreshUserData(onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            val userResult = firebaseSource.getCurrentUser()
            if (userResult.isSuccess) {
                val user = userResult.getOrNull()
                _currentUserFlow.value = user
                if (user != null) {
                    targetUidFlow.value = when {
                        user.role == "caregiver" && user.connectedPatientUid.isNotEmpty() && !_isLocalMode.value -> user.connectedPatientUid
                        user.role == "family" -> user.caregiverUid
                        else -> user.uid
                    }
                }
            }
            onComplete?.invoke()
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
        val user = _currentUserFlow.value
        val validPhotoUrl = if (!photoUrl.isNullOrEmpty() && user != null && !user.isPremium) {
            _statusMessage.value = "Fitur upload foto bukti minum obat hanya untuk akun Caregiver Premium!"
            null
        } else {
            if (isTakenToday && !photoUrl.isNullOrEmpty()) {
                _statusMessage.value = "Bukti foto berhasil diunggah (Premium)"
            } else if (isTakenToday) {
                _statusMessage.value = "Status obat diperbarui: Sudah Diminum"
            } else {
                _statusMessage.value = "Status obat diperbarui: Belum Diminum"
            }
            photoUrl
        }

        viewModelScope.launch {
            try {
                val uid = targetUidFlow.value
                if (uid != null) {
                    medRepository.setMedicationTakenStatus(uid, medication.id, isTakenToday, validPhotoUrl)
                    
                    // Trigger AI Verification if photo was uploaded
                    if (isTakenToday && !photoUrl.isNullOrEmpty()) {
                        try {
                            val app = getApplication<com.mdp.caremate.CareMateApplication>()
                            val premiumRepo = app.premiumRepository
                            val alert = premiumRepo.verifyMedication(photoUrl, medication.name)
                            
                            // Save to Firestore so family can see. 
                            // IMPORTANT: MUST use actual caregiver UID, not the patient's UID.
                            val actualCaregiverUid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
                            val finalAlert = alert.copy(caregiverId = actualCaregiverUid)
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

    fun toggleCaregiverSyncMode(toLocal: Boolean, onSuccess: () -> Unit) {
        val user = _currentUserFlow.value ?: return
        _isLocalMode.value = toLocal
        if (toLocal || user.connectedPatientUid.isEmpty()) {
            targetUidFlow.value = user.uid
        } else {
            targetUidFlow.value = user.connectedPatientUid
        }
        onSuccess()
    }
}
