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
        name: String,
        jobTitle: String,
        age: Int,
        bio: String,
        experience: List<String>,
        skills: List<String>
    ): Result<String> {
        return firebaseSource.updateUserProfile(name, jobTitle, age, bio, experience, skills)
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

    // New functions for Family Profile
    override suspend fun updateUsername(newName: String): Result<String> {
        return firebaseSource.updateUsername(newName)
    }

    override suspend fun changePassword(
        currentPassword: String,
        newPassword: String
    ): Result<String> {
        return firebaseSource.changePassword(currentPassword, newPassword)
    }

    override suspend fun uploadProfilePhoto(photoBase64: String): Result<String> {
        return firebaseSource.uploadProfilePhoto(photoBase64)
    }
}
