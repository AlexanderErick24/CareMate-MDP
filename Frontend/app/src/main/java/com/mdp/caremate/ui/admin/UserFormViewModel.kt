package com.mdp.caremate.ui.admin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mdp.caremate.data.model.User

class UserFormViewModel : ViewModel() {

    private val _toastMessage = MutableLiveData<String>()
    val toastMessage: LiveData<String> get() = _toastMessage

    private val _isSaveSuccess = MutableLiveData<Boolean>()
    val isSaveSuccess: LiveData<Boolean> get() = _isSaveSuccess

    private val _isDeleteSuccess = MutableLiveData<Boolean>()
    val isDeleteSuccess: LiveData<Boolean> get() = _isDeleteSuccess

    /**
     * Logika untuk memvalidasi dan menyimpan data User
     */
    fun saveUser(
        uid: String,
        name: String,
        email: String,
        role: String,
        pairingCode: String,
        caregiverUid: String
    ) {
        // Validasi Input
        if (name.isEmpty() || email.isEmpty() || role.isEmpty()) {
            _toastMessage.value = "Nama, Email, dan Role wajib diisi!"
            return
        }

        // Mapping ke objek User
        val user = User(
            uid = uid.ifEmpty { "GENERATED_ID_${System.currentTimeMillis()}" }, // Contoh ID otomatis jika baru
            name = name,
            email = email,
            role = role,
            pairingCode = pairingCode,
            caregiverUid = caregiverUid
        )

        // TODO: Hubungkan ke Repository / Firebase di sini
        // repository.saveUser(user)

        _toastMessage.value = "Data ${user.name} berhasil disimpan!"
        _isSaveSuccess.value = true
    }

    /**
     * Logika untuk menghapus data User
     */
    fun deleteUser(uid: String) {
        if (uid.isEmpty()) {
            _toastMessage.value = "User ID tidak valid!"
            return
        }

        // TODO: Hubungkan ke Repository / Firebase untuk hapus data
        // repository.deleteUser(uid)

        _toastMessage.value = "Data pengguna berhasil dihapus!"
        _isDeleteSuccess.value = true
    }
}