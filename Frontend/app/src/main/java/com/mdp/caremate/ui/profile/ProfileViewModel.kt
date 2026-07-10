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
        if (name.trim().isEmpty()) {
            _toastMessage.value = "Nama tidak boleh kosong"
            return
        }
        if (age < 0 || age > 120) {
            _toastMessage.value = "Usia harus bernilai valid (0 - 120 tahun)"
            return
        }
        if (bio.length > 500) {
            _toastMessage.value = "Bio maksimal 500 karakter"
            return
        }
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

    // =========================
    // NEW: For Family Profile
    // =========================

    fun updateUsername(newName: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.updateUsername(newName)
            if (result.isSuccess) {
                _toastMessage.value = "Nama berhasil diperbarui!"
                fetchCurrentUser() // refresh display
            } else {
                _toastMessage.value = "Gagal: ${result.exceptionOrNull()?.message}"
            }
            _isLoading.value = false
        }
    }

    fun changePassword(currentPassword: String, newPassword: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.changePassword(currentPassword, newPassword)
            if (result.isSuccess) {
                _toastMessage.value = "Password berhasil diubah!"
            } else {
                _toastMessage.value = "Gagal: ${result.exceptionOrNull()?.message}"
            }
            _isLoading.value = false
        }
    }

    fun uploadProfilePhoto(photoBase64: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.uploadProfilePhoto(photoBase64)
            if (result.isSuccess) {
                _toastMessage.value = "Foto profil berhasil diperbarui!"
                fetchCurrentUser() // refresh display
            } else {
                _toastMessage.value = "Gagal: ${result.exceptionOrNull()?.message}"
            }
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
