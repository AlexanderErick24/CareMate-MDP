package com.mdp.caremate.ui.family.management

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

import com.mdp.caremate.data.repositories.AuthRepository

class FamilyManagementViewModelFactory(

    private val repository: AuthRepository

) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        return FamilyManagementViewModel(
            repository
        ) as T
    }
}