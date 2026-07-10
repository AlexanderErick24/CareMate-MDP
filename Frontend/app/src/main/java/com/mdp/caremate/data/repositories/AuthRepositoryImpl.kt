package com.mdp.caremate.data.repositories

import com.mdp.caremate.data.model.FamilyMember
import com.mdp.caremate.data.model.QuitRequest
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

    override suspend fun getFamilyMembers(): Result<List<String>> {
        return firebaseSource.getFamilyMembers()
    }

    override suspend fun getFamilyMemberList(): Result<List<FamilyMember>> {
        return firebaseSource.getFamilyMemberList()
    }

    // ==================================================
    // QUIT SYSTEM
    // ==================================================

    override suspend fun sendCaregiverQuitRequest(): Result<String> {
        return firebaseSource.sendCaregiverQuitRequest()
    }

    override suspend fun sendFamilyQuitRequest(): Result<String> {
        return firebaseSource.sendFamilyQuitRequest()
    }

    override suspend fun getPendingCaregiverQuitRequest(): Result<QuitRequest?> {
        return firebaseSource.getPendingCaregiverQuitRequest()
    }

    override suspend fun getPendingCaregiverQuitRequestForFamily(): Result<QuitRequest?> {
        return firebaseSource.getPendingCaregiverQuitRequestForFamily()
    }

    override suspend fun getPendingFamilyQuitRequestForCaregiver(): Result<QuitRequest?> {
        return firebaseSource.getPendingFamilyQuitRequestForCaregiver()
    }

    override suspend fun getPendingFamilyQuitRequestForSelf(): Result<QuitRequest?> {
        return firebaseSource.getPendingFamilyQuitRequestForSelf()
    }

    override suspend fun approveCaregiverQuit(
        requestId: String,
        newPatientName: String,
        newPairingCode: String
    ): Result<Unit> {
        return firebaseSource.approveCaregiverQuit(
            requestId,
            newPatientName,
            newPairingCode
        )
    }

    override suspend fun rejectCaregiverQuit(
        requestId: String,
        reason: String
    ): Result<Unit> {
        return firebaseSource.rejectCaregiverQuit(requestId, reason)
    }

    override suspend fun approveFamilyQuit(requestId: String): Result<Unit> {
        return firebaseSource.approveFamilyQuit(requestId)
    }

    override suspend fun rejectFamilyQuit(
        requestId: String,
        reason: String
    ): Result<Unit> {
        return firebaseSource.rejectFamilyQuit(requestId, reason)
    }

    override suspend fun reconnectFamilyToNewCaregiver(newPairingCode: String): Result<Unit> {
        return firebaseSource.reconnectFamilyToNewCaregiver(newPairingCode)
    }

    override suspend fun dismissQuitRequest(requestId: String): Result<Unit> {
        return firebaseSource.dismissQuitRequest(requestId)
    }
}