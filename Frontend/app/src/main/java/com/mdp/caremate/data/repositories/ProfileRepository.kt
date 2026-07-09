package com.mdp.caremate.data.repositories

import com.mdp.caremate.data.model.FamilyRequest
import com.mdp.caremate.data.model.User

interface ProfileRepository {
    suspend fun getCurrentUser(): Result<User>
    suspend fun updateUserProfile(name: String, jobTitle: String, age: Int, bio: String, experience: List<String>, skills: List<String>): Result<String>
    suspend fun updateProfilePhoto(photoUrl: String): Result<String>
    suspend fun requestJoinFamily(targetCode: String): Result<String>
    suspend fun acceptJoinRequest(requestId: String, caregiverUid: String): Result<String>
    suspend fun rejectJoinRequest(requestId: String): Result<String>
}
