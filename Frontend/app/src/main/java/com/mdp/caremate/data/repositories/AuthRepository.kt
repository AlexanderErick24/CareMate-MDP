package com.mdp.caremate.data.repositories

import com.mdp.caremate.data.model.FamilyMember
import com.mdp.caremate.data.model.QuitRequest
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
    suspend fun getFamilyMembers(): Result<List<String>>

    suspend fun getFamilyMemberList(): Result<List<FamilyMember>>

    // ==================================================
    // QUIT SYSTEM
    // ==================================================

    suspend fun sendCaregiverQuitRequest(): Result<String>
    suspend fun sendFamilyQuitRequest(): Result<String>

    suspend fun getPendingCaregiverQuitRequest(): Result<QuitRequest?>
    suspend fun getPendingCaregiverQuitRequestForFamily(): Result<QuitRequest?>
    suspend fun getPendingFamilyQuitRequestForCaregiver(): Result<QuitRequest?>
    suspend fun getPendingFamilyQuitRequestForSelf(): Result<QuitRequest?>

    suspend fun approveCaregiverQuit(
        requestId: String,
        newPatientName: String,
        newPairingCode: String
    ): Result<Unit>

    suspend fun rejectCaregiverQuit(
        requestId: String,
        reason: String
    ): Result<Unit>

    suspend fun approveFamilyQuit(requestId: String): Result<Unit>

    suspend fun rejectFamilyQuit(
        requestId: String,
        reason: String
    ): Result<Unit>

    suspend fun reconnectFamilyToNewCaregiver(newPairingCode: String): Result<Unit>
    suspend fun dismissQuitRequest(requestId: String): Result<Unit>
}