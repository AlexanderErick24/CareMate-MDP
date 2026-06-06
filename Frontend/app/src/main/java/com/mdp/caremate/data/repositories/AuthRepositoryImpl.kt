package com.mdp.caremate.data.repositories

import com.mdp.caremate.data.sources.remote.FirebaseSource

class AuthRepositoryImpl(
    private val firebaseSource: FirebaseSource
) : AuthRepository {

    override suspend fun registerCaregiver(
        name: String,
        email: String,
        password: String
    ): Result<String> {

        return firebaseSource.registerCaregiver(
            name,
            email,
            password
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
}