package com.mdp.caremate.ui.medicationform

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.mdp.caremate.data.model.Medication
import com.mdp.caremate.data.repositories.MedRepository
import com.mdp.caremate.data.repositories.MedRepositoryImpl
import com.mdp.caremate.data.sources.local.MedicationAlarmScheduler
import com.mdp.caremate.data.sources.local.AppDatabase
import kotlinx.coroutines.launch

class MedFormViewModel(application: Application) : AndroidViewModel(application) {
    private val medRepository: MedRepository = MedRepositoryImpl(
        AppDatabase.getDatabase(application).medicationDao()
    )

    private val _selectedMedication = MutableLiveData<Medication?>(null)
    val selectedMedication: LiveData<Medication?> = _selectedMedication

    private val _isEditing = MutableLiveData(false)
    val isEditing: LiveData<Boolean> = _isEditing

    private val _message = MutableLiveData<String?>()
    val message: LiveData<String?> = _message

    private val _closeScreen = MutableLiveData(false)
    val closeScreen: LiveData<Boolean> = _closeScreen

    private var currentMedicationId: Long = -1L
    private val scheduler = MedicationAlarmScheduler(application)

    fun loadMedication(medicationId: Long) {
        if (medicationId <= 0) return
        viewModelScope.launch {
            val medication = medRepository.getMedicationById(medicationId)
            if (medication != null) {
                currentMedicationId = medicationId
                _selectedMedication.value = medication
                _isEditing.value = true
            } else {
                _message.value = "Data obat tidak ditemukan."
            }
        }
    }

    fun saveMedication(
        name: String,
        dosage: String,
        hourText: String,
        minuteText: String
    ) {
        val trimmedName = name.trim()
        val trimmedDosage = dosage.trim()
        val hour = hourText.trim().toIntOrNull()
        val minute = minuteText.trim().toIntOrNull()

        if (trimmedName.isEmpty() || trimmedDosage.isEmpty() || hour == null || minute == null) {
            _message.value = "Semua field wajib diisi."
            return
        }

        if (hour !in 0..23 || minute !in 0..59) {
            _message.value = "Jam harus 0-23 dan menit 0-59."
            return
        }

        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val medication = Medication(
                id = currentMedicationId.takeIf { it > 0 } ?: 0L,
                name = trimmedName,
                dosage = trimmedDosage,
                intakeHour = hour,
                intakeMinute = minute,
                isTakenToday = false,
                isEnabled = true,
                createdAt = selectedMedication.value?.createdAt ?: now,
                updatedAt = now
            )

            if (currentMedicationId > 0) {
                val updatedMedication = medRepository.updateMedication(medication)
                scheduler.schedule(updatedMedication)
                _message.value = "Jadwal obat berhasil diperbarui."
            } else {
                val insertedMedication = medRepository.insertMedication(medication)
                scheduler.schedule(insertedMedication)
                _message.value = "Jadwal obat berhasil disimpan."
            }
            _closeScreen.value = true
        }
    }

    fun deleteMedication() {
        if (currentMedicationId <= 0) return
        viewModelScope.launch {
            medRepository.deleteMedicationById(currentMedicationId)
            scheduler.cancel(currentMedicationId)
            _message.value = "Jadwal obat berhasil dihapus."
            _closeScreen.value = true
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    fun consumeCloseScreen() {
        _closeScreen.value = false
    }
}
