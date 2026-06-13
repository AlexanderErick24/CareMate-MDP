package com.mdp.caremate.data.repositories

import com.mdp.caremate.data.model.User

interface AuthRepository {

    suspend fun register(
        name: String,
        email: String,
        password: String,
        role: String,
        pairingCode: String,
        patientName: String
    ): Result<String>

    suspend fun login(
        email: String,
        password: String
    ): Result<String>

    suspend fun getCurrentUser(): Result<User>
    suspend fun getLinkedCaregiver(): Result<User>
}