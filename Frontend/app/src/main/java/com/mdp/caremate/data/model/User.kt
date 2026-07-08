package com.mdp.caremate.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val password: String = "",
    val role: String = "",
    val pairingCode: String = "",
    val caregiverUid: String = "",
    val patientName: String = "",
    
    // Fitur Profil CV
    val jobTitle: String = "",
    val age: Int = 0,
    val bio: String = "",
    val experience: List<String> = emptyList(),
    val skills: List<String> = emptyList(),
    val photoUrl: String = "",
    
    // Fitur Gabung Keluarga
    val connectedPatientUid: String = "",

    // Fitur Admin
    val status: Boolean = true, // Enable / Disable

    // Fitur Foto Profil
    val photoUrl: String = ""

): Parcelable