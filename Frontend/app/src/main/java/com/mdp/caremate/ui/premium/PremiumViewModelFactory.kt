package com.mdp.caremate.ui.premium

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import com.mdp.caremate.CareMateApplication

// Menggunakan object (singleton) seperti contoh di kelas
val PremiumViewModelFactory = object : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(
        modelClass: Class<T>,
        extras: CreationExtras
    ): T =
        with(modelClass) {
            // Ambil Application dan ubah menjadi CareMateApplication
            val application = checkNotNull(extras[APPLICATION_KEY]) as CareMateApplication

            // Ambil repository yang sudah disiapkan di Application
            val premiumRepository = application.premiumRepository

            when {
                isAssignableFrom(PremiumViewModel::class.java) -> PremiumViewModel(premiumRepository)
                isAssignableFrom(com.mdp.caremate.ui.family.alert.AiAlertViewModel::class.java) -> com.mdp.caremate.ui.family.alert.AiAlertViewModel(premiumRepository)

                // Kalau nanti Edo butuh Factory untuk Medication, bisa ditambah di sini:
                // isAssignableFrom(MedicationViewModel::class.java) -> MedicationViewModel(application.medicationRepository)

                else ->
                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        } as T
}