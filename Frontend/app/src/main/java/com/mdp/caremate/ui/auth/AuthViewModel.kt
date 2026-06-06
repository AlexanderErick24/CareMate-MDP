package com.mdp.caremate.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

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

    fun registerCaregiver(
        name: String,
        email: String,
        password: String
    ) {

        viewModelScope.launch {

            val result =
                repository.registerCaregiver(
                    name,
                    email,
                    password
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
}