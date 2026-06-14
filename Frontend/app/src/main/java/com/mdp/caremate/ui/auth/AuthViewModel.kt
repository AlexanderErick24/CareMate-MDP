package com.mdp.caremate.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
}