package com.mdp.caremate.data.repositories

import com.google.firebase.firestore.ListenerRegistration
import com.mdp.caremate.data.model.ChatMessage
import com.mdp.caremate.data.sources.remote.FirebaseSource

class ChatRepositoryImpl(

    private val firebaseSource:
    FirebaseSource

) : ChatRepository {

    override suspend fun sendMessage(
        pairingCode: String,
        message: String
    ) =
        firebaseSource.sendMessage(
            pairingCode,
            message
        )

    override suspend fun getPairingCodeForCurrentUser() =
        firebaseSource.getPairingCodeForCurrentUser()

    override fun observeMessages(

        pairingCode: String,

        onMessagesChanged:
            (List<ChatMessage>) -> Unit

    ): ListenerRegistration {

        return firebaseSource.observeMessages(
            pairingCode,
            onMessagesChanged
        )
    }
}