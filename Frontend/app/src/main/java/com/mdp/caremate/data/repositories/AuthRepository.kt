package com.mdp.caremate.data.repositories

interface AuthRepository {

    suspend fun registerCaregiver(
        name: String,
        email: String,
        password: String
    ): Result<String>

    suspend fun login(
        email: String,
        password: String
    ): Result<String>
}