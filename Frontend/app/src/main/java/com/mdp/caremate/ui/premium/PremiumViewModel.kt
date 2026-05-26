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

    // ==========================================
    // 2. STATE UNTUK RIWAYAT JURNAL (HISTORY)
    // ==========================================
    private val _journalList = ArrayList<Journal>()
    private val _journals = MutableLiveData<List<Journal>>(_journalList.toList())
    val journals: LiveData<List<Journal>>
        get() = _journals

    // Fungsi untuk memuat awal data riwayat
    fun initHistory() {
        viewModelScope.launch {
            refreshHistoryList()
        }
    }

    // Fungsi internal untuk menyegarkan daftar riwayat
    private suspend fun refreshHistoryList() {
        _journalList.clear()

        // TODO: Nanti kita buka komentar di bawah ini setelah kita buat fungsi getAllJournals()
        // di dalam PremiumRepository (Entah mengambil dari Room lokal atau Firebase).
        // _journalList.addAll(repository.getAllJournals())
        _journals.value = _journalList.toList()
    }

    // ==========================================
    // 3. FUNGSI UTAMA (TOMBOL ANALISIS)
    // ==========================================
    fun analyzeJournal(content: String) {
        if (content.trim().isEmpty()) {
            _errorMessage.value = "Teks jurnal tidak boleh kosong!"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Tembak API AI
                val result = repository.analyzeMood(content)
                _journalResult.value = result

                // TODO: Nanti tambahkan repository.insert(result) di sini untuk menyimpan ke database lokal
                // Setelah berhasil dianalisis & disimpan, perbarui daftar riwayat!
                refreshHistoryList()

            } catch (e: Exception) {
                _errorMessage.value = "Gagal menganalisis: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

}
