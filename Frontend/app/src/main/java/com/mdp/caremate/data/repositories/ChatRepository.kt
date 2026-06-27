package com.mdp.caremate.data.repositories

import com.google.firebase.firestore.ListenerRegistration
import com.mdp.caremate.data.model.ChatMessage

interface ChatRepository {

    suspend fun sendMessage(
        pairingCode: String,
        message: String
    ): Result<Unit>

    suspend fun getPairingCodeForCurrentUser():
            Result<String>

    fun observeMessages(

        pairingCode: String,

        onMessagesChanged:
            (List<ChatMessage>) -> Unit

    ): ListenerRegistration
}