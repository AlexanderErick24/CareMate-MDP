package com.mdp.caremate.ui.chat

import androidx.lifecycle.*

import com.google.firebase.auth.FirebaseAuth
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

    // NEW: how many unread messages exist for the current user.
    // 0 means no badge. > 0 means show badge.
    // Only meaningful if lastSenderId != currentUid — checked in the Fragment.
    private val _unreadCount =
        MutableLiveData<Int>()

    val unreadCount: LiveData<Int>
        get() = _unreadCount

    // NEW: the UID of the person who sent the last message.
    // The Fragment uses this to decide whether to show the badge
    // (badge only appears for the OTHER user, not the sender).
    private val _lastSenderId =
        MutableLiveData<String>()

    val lastSenderId: LiveData<String>
        get() = _lastSenderId

    private var messagesListener:
            ListenerRegistration? = null

    // NEW: listener for the chatRoom document (for badge)
    private var roomListener:
            ListenerRegistration? = null

    fun loadChatRoom() {

        viewModelScope.launch {

            repository
                .getPairingCodeForCurrentUser()
                .onSuccess { code ->

                    _pairingCode.value = code

                    observeMessages(code)

                    // NEW: also observe the room document for badge data
                    observeRoom(code)
                }
        }
    }

    private fun observeMessages(
        pairingCode: String
    ) {

        messagesListener?.remove()

        messagesListener =
            repository.observeMessages(
                pairingCode
            ) {

                _messages.postValue(it)
            }
    }

    // NEW: listen to the chatRoom document for unreadCount + lastSenderId
    private fun observeRoom(
        pairingCode: String
    ) {

        roomListener?.remove()

        roomListener =
            repository.observeChatRoom(
                pairingCode
            ) { room ->

                _unreadCount.postValue(room.unreadCount)
                _lastSenderId.postValue(room.lastSenderId)
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

    // NEW: call this when the user opens ChatFragment to clear the badge
    fun markAsRead() {

        val code = pairingCode.value

        if (code != null) {

            viewModelScope.launch {
                repository.markChatAsRead(code)
            }

        } else {

            // pairingCode not loaded yet — load room first then mark read
            viewModelScope.launch {

                repository
                    .getPairingCodeForCurrentUser()
                    .onSuccess { code ->

                        _pairingCode.value = code
                        repository.markChatAsRead(code)
                    }
            }
        }
    }

    override fun onCleared() {

        messagesListener?.remove()
        roomListener?.remove()

        super.onCleared()
    }
}