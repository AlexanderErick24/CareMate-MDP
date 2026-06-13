package com.mdp.caremate.data.repositories

import com.mdp.caremate.data.model.User
import com.mdp.caremate.data.sources.remote.FirebaseSource

class ProfileRepositoryImpl(
    private val firebaseSource: FirebaseSource
) : ProfileRepository {

    override suspend fun getCurrentUser(): Result<User> {
        return firebaseSource.getCurrentUser()
    }

    override suspend fun updateUserProfile(
        jobTitle: String,
        age: Int,
        bio: String,
        experience: List<String>,
        skills: List<String>
    ): Result<String> {
        return firebaseSource.updateUserProfile(jobTitle, age, bio, experience, skills)
    }

    override suspend fun requestJoinFamily(targetCode: String): Result<String> {
        return firebaseSource.requestJoinFamily(targetCode)
    }

    override suspend fun acceptJoinRequest(
        requestId: String,
        caregiverUid: String
    ): Result<String> {
        return firebaseSource.acceptJoinRequest(requestId, caregiverUid)
    }

    override suspend fun rejectJoinRequest(requestId: String): Result<String> {
        return firebaseSource.rejectJoinRequest(requestId)
    }
}
