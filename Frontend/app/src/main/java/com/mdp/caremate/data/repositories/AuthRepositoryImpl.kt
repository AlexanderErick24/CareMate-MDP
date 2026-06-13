package com.mdp.caremate.data.repositories

import com.mdp.caremate.data.model.User
import com.mdp.caremate.data.sources.remote.FirebaseSource

class AuthRepositoryImpl(
    private val firebaseSource: FirebaseSource
) : AuthRepository {

    override suspend fun register(
        name: String,
        email: String,
        password: String,
        role: String,
        pairingCode: String,
        patientName: String
    ): Result<String> {

        return firebaseSource.register(
            name,
            email,
            password,
            role,
            pairingCode,
            patientName
        )
    }

    override suspend fun login(
        email: String,
        password: String
    ): Result<String> {

        return firebaseSource.login(
            email,
            password
        )
    }

    override suspend fun getCurrentUser(): Result<User> {
        return firebaseSource.getCurrentUser()
    }

    override suspend fun getLinkedCaregiver(): Result<User> {
        return firebaseSource.getLinkedCaregiver()
    }
}