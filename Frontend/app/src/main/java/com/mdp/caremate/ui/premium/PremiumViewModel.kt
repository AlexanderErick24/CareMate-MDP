package com.mdp.caremate.ui.premium

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mdp.caremate.data.model.Journal
import com.mdp.caremate.data.repositories.PremiumRepository
import com.mdp.caremate.data.sources.remote.ChatHistoryItem
import com.mdp.caremate.data.sources.remote.ChatHistoryPart
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
                // 1. Susun riwayat percakapan dari jurnal yang sudah ada di lokal
                // agar AI "mengingat" konteks percakapan sebelumnya
                val history = _journalList.flatMap { journal ->
                    val items = mutableListOf<ChatHistoryItem>()
                    if (journal.content.isNotBlank()) {
                        items.add(ChatHistoryItem("user", listOf(ChatHistoryPart(journal.content))))
                    }
                    if (journal.aiAnalysis.isNotBlank()) {
                        items.add(ChatHistoryItem("model", listOf(ChatHistoryPart(journal.aiAnalysis))))
                    }
                    items
                }

                // 2. Tembak API AI dengan konten + riwayat
                val result = repository.analyzeMood(content, history)

                // 3. Kunci caregiverId dan kembalikan content asli
                val finalResult = result.copy(caregiverId = caregiverId, content = content)

                _journalResult.value = finalResult

                // 4. Simpan ke Database Lokal
                repository.insertJournal(finalResult)

                // 5. Pemicu Alert Burnout
                _burnoutAlert.value = finalResult.moodScore <= 3

                // 6. Perbarui daftar riwayat
                refreshHistoryList(caregiverId)

            } catch (e: Exception) {
                _errorMessage.value = "Gagal menganalisis: ${e.message}"
                _errorMessage.value = null // Cegah re-emisi saat layar diputar
                
                // Refresh list untuk menghapus pesan "Ghost Loading Bubble" di UI 
                // akibat pemanggilan adapter secara manual yang terputus di tengah jalan
                refreshHistoryList(caregiverId)
            } finally {
                _isLoading.value = false
            }
        }
    }

}
