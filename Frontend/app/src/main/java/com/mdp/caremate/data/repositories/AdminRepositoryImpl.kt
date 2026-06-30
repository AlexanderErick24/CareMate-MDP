package com.mdp.caremate.data.repositories

import com.mdp.caremate.data.model.Event
import com.mdp.caremate.data.model.User
import com.mdp.caremate.data.sources.remote.FirebaseSource

class AdminRepositoryImpl(
    private val firebaseSource: FirebaseSource
) : AdminRepository {

    override suspend fun getAllUser(): List<User> {
        return firebaseSource.getAllUser()
    }

    override suspend fun getAllEvent(): List<Event> {
        return firebaseSource.getAllEvent()
    }

    override suspend fun getUserByID(id: String): User? {
        return firebaseSource.getUserByID(id)
    }
}