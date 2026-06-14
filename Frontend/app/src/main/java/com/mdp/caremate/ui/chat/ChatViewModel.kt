package com.mdp.caremate.ui.chat

import androidx.lifecycle.*

import com.google.firebase.firestore.ListenerRegistration
import com.mdp.caremate.data.model.ChatMessage
import com.mdp.caremate.data.repositories.ChatRepository

import kotlinx.coroutines.launch

class ChatViewModel(

    private val repository:
    ChatRepository

) : ViewModel() {

    private val _messages =
        MutableLiveData<List<ChatMessage>>()

    val messages:
            LiveData<List<ChatMessage>>
        get() = _messages

    private val _pairingCode =
        MutableLiveData<String>()

    val pairingCode:
            LiveData<String>
        get() = _pairingCode

    private var listener:
            ListenerRegistration? = null

    fun loadChatRoom() {

        viewModelScope.launch {

            repository
                .getPairingCodeForCurrentUser()
                .onSuccess {

                    _pairingCode.value = it

                    observeMessages(it)
                }
        }
    }

    private fun observeMessages(
        pairingCode: String
    ) {

        listener?.remove()

        listener =
            repository.observeMessages(
                pairingCode
            ) {

                _messages.postValue(it)
            }
    }

    fun sendMessage(
        message: String
    ) {

        val room =
            pairingCode.value ?: return

        viewModelScope.launch {

            repository.sendMessage(
                room,
                message
            )
        }
    }

    override fun onCleared() {

        listener?.remove()

        super.onCleared()
    }
}