package com.mdp.caremate.data.model

data class ChatRoom(
    val pairingCode: String = "",
    val patientName: String = "",
    val caregiverUid: String = "",

    // UID of the person who sent the last message.
    // Used to decide whose turn it is to see a badge.
    // If lastSenderId != currentUid → show unread badge.
    val lastSenderId: String = "",

    // Incremented every time a message is sent.
    // Reset to 0 when the OTHER user opens ChatFragment.
    val unreadCount: Int = 0
)