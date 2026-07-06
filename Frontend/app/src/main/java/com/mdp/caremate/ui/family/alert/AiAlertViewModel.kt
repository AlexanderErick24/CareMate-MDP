package com.mdp.caremate.ui.family.alert

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mdp.caremate.data.model.AiAlert
import com.mdp.caremate.data.repositories.PremiumRepository
import kotlinx.coroutines.launch

class AiAlertViewModel(
    private val repository: PremiumRepository
) : ViewModel() {

    private val _alerts = MutableLiveData<List<AiAlert>>()
    val alerts: LiveData<List<AiAlert>> = _alerts

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun fetchAlerts() {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.getAiAlertsForFamily()
            if (result.isSuccess) {
                _alerts.value = result.getOrNull() ?: emptyList()
            } else {
                _errorMessage.value = result.exceptionOrNull()?.message ?: "Gagal memuat data"
            }
            _isLoading.value = false
        }
    }
}
