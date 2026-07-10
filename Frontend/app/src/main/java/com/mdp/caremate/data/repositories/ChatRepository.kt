package com.mdp.caremate.data.repositories

import com.google.firebase.firestore.ListenerRegistration
import com.mdp.caremate.data.model.ChatMessage
import com.mdp.caremate.data.model.ChatRoom

interface ChatRepository {

    suspend fun sendMessage(
        pairingCode: String,
        message: String
    ): Result<Unit>

    suspend fun getPairingCodeForCurrentUser():
            Result<String>

    fun observeMessages(
        pairingCode: String,
        onMessagesChanged: (List<ChatMessage>) -> Unit
    ): ListenerRegistration

    // NEW: observe the chatRoom document (for unread badge)
    fun observeChatRoom(
        pairingCode: String,
        onChanged: (ChatRoom) -> Unit
    ): ListenerRegistration

    // NEW: reset unreadCount to 0 when the user opens the chat
    suspend fun markChatAsRead(pairingCode: String): Result<Unit>
}