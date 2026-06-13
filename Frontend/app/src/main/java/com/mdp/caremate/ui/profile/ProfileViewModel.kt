package com.mdp.caremate.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mdp.caremate.data.model.User
import com.mdp.caremate.data.repositories.ProfileRepository
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val repository: ProfileRepository
) : ViewModel() {

    private val _userState = MutableLiveData<User?>()
    val userState: LiveData<User?> get() = _userState

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> get() = _toastMessage

    fun fetchCurrentUser() {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.getCurrentUser()
            if (result.isSuccess) {
                _userState.value = result.getOrNull()
            } else {
                _toastMessage.value = "Gagal memuat profil: ${result.exceptionOrNull()?.message}"
            }
            _isLoading.value = false
        }
    }

    fun requestJoinFamily(targetCode: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.requestJoinFamily(targetCode)
            if (result.isSuccess) {
                _toastMessage.value = "Berhasil meminta gabung!"
                fetchCurrentUser() // refresh
            } else {
                _toastMessage.value = "Gagal: ${result.exceptionOrNull()?.message}"
            }
            _isLoading.value = false
        }
    }
}
