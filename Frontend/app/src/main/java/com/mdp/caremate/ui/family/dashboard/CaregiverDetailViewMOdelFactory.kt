package com.mdp.caremate.ui.family.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mdp.caremate.data.sources.remote.FirebaseSource

class CaregiverDetailViewModelFactory(
    private val firebaseSource: FirebaseSource
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CaregiverDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CaregiverDetailViewModel(firebaseSource) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}