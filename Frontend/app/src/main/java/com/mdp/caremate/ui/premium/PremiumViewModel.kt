package com.mdp.caremate.ui.premium

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mdp.caremate.data.model.Journal
import com.mdp.caremate.data.repositories.PremiumRepository
import kotlinx.coroutines.launch

class PremiumViewModel (
    private val repository: PremiumRepository ) : ViewModel(){

    // ==========================================
    // 1. STATE UNTUK ANALISIS JURNAL BARU
    // ==========================================
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    private val _journalResult = MutableLiveData<Journal>()
    val journalResult: LiveData<Journal>
        get() = _journalResult

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String>
        get() = _errorMessage

    private val _burnoutAlert = MutableLiveData<Boolean>()
    val burnoutAlert: LiveData<Boolean>
        get() = _burnoutAlert

    // ==========================================
    // 2. STATE UNTUK RIWAYAT JURNAL (HISTORY)
    // ==========================================
    private val _journalList = ArrayList<Journal>()
    private val _journals = MutableLiveData<List<Journal>>(_journalList.toList())
    val journals: LiveData<List<Journal>>
        get() = _journals

    // Fungsi untuk memuat awal data riwayat
    fun initHistory(caregiverId: String) {
        viewModelScope.launch {
            refreshHistoryList(caregiverId)
        }
    }

    // Fungsi internal untuk menyegarkan daftar riwayat
    private suspend fun refreshHistoryList(caregiverId: String) {
        _journalList.clear()
        // Sekarang repository sudah punya fungsi ini!
        _journalList.addAll(repository.getAllJournals(caregiverId))
        _journals.value = _journalList.toList()
    }

    // ==========================================
    // 3. FUNGSI UTAMA (TOMBOL ANALISIS)
    // ==========================================
    fun analyzeJournal(content: String, caregiverId: String) {
        if (content.trim().isEmpty()) {
            _errorMessage.value = "Teks jurnal tidak boleh kosong!"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 1. Tembak API AI (Internet)
                val result = repository.analyzeMood(content)
                
                // Tambahkan caregiverId ke hasil agar disimpan dengan benar
                // SERTA kembalikan 'content' menjadi teks asli pengguna (bukan prompt lengkap)
                val finalResult = result.copy(caregiverId = caregiverId, content = content)
                
                _journalResult.value = finalResult

                // 2. Simpan hasilnya ke Database Lokal (Room)
                repository.insertJournal(finalResult)

                // Pemicu Alert Kelelahan (Burnout) jika skor mood sangat rendah (stres)
                if (finalResult.moodScore <= 3) {
                    _burnoutAlert.value = true
                } else {
                    _burnoutAlert.value = false
                }

                // 3. Perbarui daftar riwayat di layar
                refreshHistoryList(caregiverId)

            } catch (e: Exception) {
                _errorMessage.value = "Gagal menganalisis: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

}
