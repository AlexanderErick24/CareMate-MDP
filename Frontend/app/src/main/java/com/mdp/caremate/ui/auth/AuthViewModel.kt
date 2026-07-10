package com.mdp.caremate.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mdp.caremate.data.model.QuitRequest
import com.mdp.caremate.data.model.User

import com.mdp.caremate.data.repositories.AuthRepository

import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _registerState =
        MutableLiveData<Result<String>>()

    val registerState: LiveData<Result<String>>
        get() = _registerState

    private val _loginState =
        MutableLiveData<Result<String>>()

    val loginState: LiveData<Result<String>>
        get() = _loginState

    private val _currentUser = MutableLiveData<User>()

    val currentUser: LiveData<User>
        get() = _currentUser

    private val _linkedCaregiver =
        MutableLiveData<User>()

    val linkedCaregiver: LiveData<User>
        get() = _linkedCaregiver

    private val _familyMembers =
        MutableLiveData<List<String>>()

    val familyMembers:
            LiveData<List<String>>
        get() = _familyMembers

    // ==================================================
    // QUIT SYSTEM
    // ==================================================

    // Holds the current caregiver_quit request status (for ProfileFragment)
    private val _caregiverQuitRequest =
        MutableLiveData<QuitRequest?>()

    val caregiverQuitRequest: LiveData<QuitRequest?>
        get() = _caregiverQuitRequest

    // Holds the pending caregiver_quit request visible to a family member
    private val _caregiverQuitRequestForFamily =
        MutableLiveData<QuitRequest?>()

    val caregiverQuitRequestForFamily: LiveData<QuitRequest?>
        get() = _caregiverQuitRequestForFamily

    // Holds the pending family_quit request visible to the caregiver
    private val _familyQuitRequestForCaregiver =
        MutableLiveData<QuitRequest?>()

    val familyQuitRequestForCaregiver: LiveData<QuitRequest?>
        get() = _familyQuitRequestForCaregiver

    // Holds the pending family_quit request for the current family member (self)
    private val _familyQuitRequestForSelf =
        MutableLiveData<QuitRequest?>()

    val familyQuitRequestForSelf: LiveData<QuitRequest?>
        get() = _familyQuitRequestForSelf

    // General toast message for quit actions
    private val _quitToastMessage = MutableLiveData<String>()

    val quitToastMessage: LiveData<String>
        get() = _quitToastMessage

    // Signals that the caregiver quit was approved — caregiver should navigate to MedForm
    private val _caregiverQuitApproved = MutableLiveData<Boolean>()

    val caregiverQuitApproved: LiveData<Boolean>
        get() = _caregiverQuitApproved

    // Signals that the family quit was approved — family member should reconnect
    private val _familyQuitApproved = MutableLiveData<Boolean>()

    val familyQuitApproved: LiveData<Boolean>
        get() = _familyQuitApproved

    // ==================================================

    fun register(
        name: String,
        email: String,
        password: String,
        role: String,
        pairingCode: String,
        patientName: String
    ) {

        viewModelScope.launch {

            val result =
                repository.register(
                    name,
                    email,
                    password,
                    role,
                    pairingCode,
                    patientName
                )

            _registerState.value = result
        }
    }

    fun login(
        email: String,
        password: String
    ) {

        viewModelScope.launch {

            val result =
                repository.login(
                    email,
                    password
                )

            _loginState.value = result
        }
    }

    fun getCurrentUser() {
        viewModelScope.launch {
            repository.getCurrentUser().onSuccess {
                _currentUser.value = it
            }
        }
    }

    fun getLinkedCaregiver() {

        viewModelScope.launch {

            repository.getLinkedCaregiver()
                .onSuccess {

                    _linkedCaregiver.value = it

                }
        }
    }

    fun getFamilyMembers() {

        viewModelScope.launch {

            repository.getFamilyMembers()
                .onSuccess {

                    _familyMembers.value = it
                }
        }
    }

    // ==================================================
    // QUIT SYSTEM FUNCTIONS
    // ==================================================

    // Caregiver: send quit request
    fun sendCaregiverQuitRequest() {
        viewModelScope.launch {
            val result = repository.sendCaregiverQuitRequest()
            result.onSuccess {
                // Refresh the status after sending
                loadCaregiverQuitRequest()
            }
            result.onFailure { e ->
                if (e.message == "ALREADY_PENDING") {
                    _quitToastMessage.value =
                        "A quit request is already pending"
                } else {
                    _quitToastMessage.value =
                        "Failed to send quit request: ${e.message}"
                }
            }
        }
    }

    // Caregiver: load own quit request status
    fun loadCaregiverQuitRequest() {
        viewModelScope.launch {
            repository.getPendingCaregiverQuitRequest()
                .onSuccess {
                    _caregiverQuitRequest.value = it
                }
        }
    }

    // Caregiver: dismiss a rejected quit request
    fun dismissCaregiverQuitRequest(requestId: String) {
        viewModelScope.launch {
            repository.dismissQuitRequest(requestId)
                .onSuccess {
                    _caregiverQuitRequest.value = null
                }
        }
    }

    // Caregiver: load family quit request directed at them
    fun loadFamilyQuitRequestForCaregiver() {
        viewModelScope.launch {
            repository.getPendingFamilyQuitRequestForCaregiver()
                .onSuccess {
                    _familyQuitRequestForCaregiver.value = it
                }
        }
    }

    // Caregiver: approve a family member's quit request
    fun approveFamilyQuit(requestId: String) {
        viewModelScope.launch {
            repository.approveFamilyQuit(requestId)
                .onSuccess {
                    _familyQuitRequestForCaregiver.value = null
                    _quitToastMessage.value = "Family member disconnected"
                }
                .onFailure { e ->
                    _quitToastMessage.value =
                        "Failed to approve: ${e.message}"
                }
        }
    }

    // Caregiver: reject a family member's quit request
    fun rejectFamilyQuit(requestId: String, reason: String) {
        viewModelScope.launch {
            repository.rejectFamilyQuit(requestId, reason)
                .onSuccess {
                    _familyQuitRequestForCaregiver.value = null
                    _quitToastMessage.value = "Request rejected"
                }
                .onFailure { e ->
                    _quitToastMessage.value =
                        "Failed to reject: ${e.message}"
                }
        }
    }

    // Family: load the pending caregiver_quit request (to show approval card)
    fun loadCaregiverQuitRequestForFamily() {
        viewModelScope.launch {
            repository.getPendingCaregiverQuitRequestForFamily()
                .onSuccess {
                    _caregiverQuitRequestForFamily.value = it
                }
        }
    }

    // Family: approve the caregiver quit request
    fun approveCaregiverQuit(
        requestId: String,
        newPatientName: String,
        newPairingCode: String
    ) {
        viewModelScope.launch {
            repository.approveCaregiverQuit(
                requestId,
                newPatientName,
                newPairingCode
            )
                .onSuccess {
                    _caregiverQuitRequestForFamily.value = null
                    _caregiverQuitApproved.value = true
                }
                .onFailure { e ->
                    _quitToastMessage.value =
                        "Failed to approve: ${e.message}"
                }
        }
    }

    // Family: reject the caregiver quit request
    fun rejectCaregiverQuit(requestId: String, reason: String) {
        viewModelScope.launch {
            repository.rejectCaregiverQuit(requestId, reason)
                .onSuccess {
                    _caregiverQuitRequestForFamily.value = null
                    _quitToastMessage.value = "Request rejected"
                }
                .onFailure { e ->
                    _quitToastMessage.value =
                        "Failed to reject: ${e.message}"
                }
        }
    }

    // Family: send quit request to caregiver
    fun sendFamilyQuitRequest() {
        viewModelScope.launch {
            val result = repository.sendFamilyQuitRequest()
            result.onSuccess {
                loadFamilyQuitRequestForSelf()
            }
            result.onFailure { e ->
                if (e.message == "ALREADY_PENDING") {
                    _quitToastMessage.value =
                        "A quit request is already pending"
                } else {
                    _quitToastMessage.value =
                        "Failed to send quit request: ${e.message}"
                }
            }
        }
    }

    // Family: load own quit request status
    fun loadFamilyQuitRequestForSelf() {
        viewModelScope.launch {
            repository.getPendingFamilyQuitRequestForSelf()
                .onSuccess {
                    _familyQuitRequestForSelf.value = it
                }
        }
    }

    // Family: dismiss a rejected quit request for self
    fun dismissFamilyQuitRequest(requestId: String) {
        viewModelScope.launch {
            repository.dismissQuitRequest(requestId)
                .onSuccess {
                    _familyQuitRequestForSelf.value = null
                }
        }
    }

    // Family: reconnect to a new caregiver after quit approved
    fun reconnectFamilyToNewCaregiver(newPairingCode: String) {
        viewModelScope.launch {
            repository.reconnectFamilyToNewCaregiver(newPairingCode)
                .onSuccess {
                    _familyQuitApproved.value = true
                }
                .onFailure { e ->
                    _quitToastMessage.value =
                        "Invalid Pairing Code: ${e.message}"
                }
        }
    }

    fun consumeCaregiverQuitApproved() {
        _caregiverQuitApproved.value = false
    }

    fun consumeFamilyQuitApproved() {
        _familyQuitApproved.value = false
    }
}