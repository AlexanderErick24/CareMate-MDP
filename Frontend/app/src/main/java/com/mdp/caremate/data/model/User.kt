package com.mdp.caremate.data.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "",
    val pairingCode: String = "",
    val caregiverUid: String = ""
)