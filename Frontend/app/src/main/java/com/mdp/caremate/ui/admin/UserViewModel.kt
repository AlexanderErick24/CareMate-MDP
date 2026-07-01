package com.mdp.caremate.ui.admin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mdp.caremate.data.model.User
import com.mdp.caremate.data.sources.remote.FirebaseSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {

    private val firebaseSource = FirebaseSource()

    private val _users = MutableLiveData<List<User>>()
    val users: LiveData<List<User>> get() = _users

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    fun fetchUsers() {
        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch(Dispatchers.Main) {
            try {
                // Mengambil seluruh user dari FirebaseSource
                val result = firebaseSource.getAllUser()

                // Menyaring role Admin agar tidak masuk ke daftar manajemen
                val filteredUsers = result.filter { !it.role.equals("Admin", ignoreCase = true) }

                _users.value = filteredUsers
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Terjadi kesalahan saat memuat data"
            } finally {
                _isLoading.value = false
            }
        }
    }
}