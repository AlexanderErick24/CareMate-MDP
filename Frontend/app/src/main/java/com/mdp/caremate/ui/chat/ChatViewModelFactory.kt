package com.mdp.caremate.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

import com.mdp.caremate.data.repositories.ChatRepository

class ChatViewModelFactory(

    private val repository:
    ChatRepository

) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        return ChatViewModel(
            repository
        ) as T
    }
}