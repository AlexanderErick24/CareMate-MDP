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
import com.mdp.caremate.data.sources.local.AppDatabase
import kotlinx.coroutines.launch

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val medRepository: MedRepository = MedRepositoryImpl(
        AppDatabase.getDatabase(application).medicationDao()
    )
    private val scheduler = MedicationAlarmScheduler(application)

    val todaysMedications: LiveData<List<Medication>> =
        medRepository.observeTodaysMedications().asLiveData()

    fun updateMedicationTakenStatus(medication: Medication, isTakenToday: Boolean) {
        viewModelScope.launch {
            medRepository.setMedicationTakenStatus(medication.id, isTakenToday)
        }
    }

    fun deleteMedication(medication: Medication) {
        viewModelScope.launch {
            medRepository.deleteMedication(medication)
            scheduler.cancel(medication.id)
        }
    }
}
