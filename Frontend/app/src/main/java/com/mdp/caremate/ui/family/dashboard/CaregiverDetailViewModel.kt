package com.mdp.caremate.ui.family.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mdp.caremate.data.model.User
import com.mdp.caremate.data.sources.remote.FirebaseSource
import kotlinx.coroutines.launch

class CaregiverDetailViewModel(
    private val firebaseSource: FirebaseSource
) : ViewModel() {

    private val _caregiver = MutableLiveData<User?>()
    val caregiver: LiveData<User?> get() = _caregiver

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> get() = _errorMessage

    fun loadCaregiver(uid: String) {
        viewModelScope.launch {
            val user = firebaseSource.getUserByID(uid)
            if (user != null) {
                _caregiver.value = user
            } else {
                _errorMessage.value = "Gagal memuat profil caregiver"
            }
        }
    }
}