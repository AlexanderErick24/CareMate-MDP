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

    private val _updateSuccess = MutableLiveData<Boolean>()
    val updateSuccess: LiveData<Boolean> get() = _updateSuccess

    fun resetUpdateSuccess() {
        _updateSuccess.value = false
    }

    fun updateUserProfile(
        name: String,
        jobTitle: String,
        age: Int,
        bio: String,
        experience: List<String>,
        skills: List<String>
    ) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.updateUserProfile(name, jobTitle, age, bio, experience, skills)
            if (result.isSuccess) {
                _toastMessage.value = "Profil berhasil diperbarui!"
                fetchCurrentUser()
                _updateSuccess.value = true
            } else {
                _toastMessage.value = "Gagal memperbarui profil: ${result.exceptionOrNull()?.message}"
            }
            _isLoading.value = false
        }
    }

    fun updateProfilePhoto(photoUrl: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.updateProfilePhoto(photoUrl)
            if (result.isSuccess) {
                _toastMessage.value = "Foto profil berhasil diperbarui!"
                fetchCurrentUser()
            } else {
                _toastMessage.value = "Gagal memperbarui foto: ${result.exceptionOrNull()?.message}"
            }
            _isLoading.value = false
        }
    }
}
