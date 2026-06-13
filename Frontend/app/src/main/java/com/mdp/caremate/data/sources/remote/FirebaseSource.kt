package com.mdp.caremate.data.sources.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mdp.caremate.data.model.User
import kotlinx.coroutines.tasks.await


class FirebaseSource {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun register(
        name: String,
        email: String,
        password: String,
        role: String,
        pairingCode: String,
        patientName: String
    ): Result<String> {

        return try {

            if (role == "caregiver") {

                registerCaregiver(
                    name,
                    email,
                    password,
                    patientName
                )

            } else {

                registerFamily(
                    name,
                    email,
                    password,
                    pairingCode
                )
            }

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    private suspend fun registerCaregiver(
        name: String,
        email: String,
        password: String,
        patientName: String
    ): Result<String> {

        val authResult =
            auth.createUserWithEmailAndPassword(
                email,
                password
            ).await()

        val uid =
            authResult.user?.uid ?: ""

        val generatedCode =
            generatePairingCode()

        val user = User(
            uid = uid,
            name = name,
            email = email,
            role = "caregiver",
            pairingCode = generatedCode,
            patientName = patientName
        )

        firestore.collection("users")
            .document(uid)
            .set(user)
            .await()

        return Result.success(generatedCode)
    }

    private suspend fun registerFamily(
        name: String,
        email: String,
        password: String,
        pairingCode: String
    ): Result<String> {

        val caregiverQuery =
            firestore.collection("users")
                .whereEqualTo(
                    "pairingCode",
                    pairingCode
                )
                .get()
                .await()

        if (caregiverQuery.isEmpty) {

            return Result.failure(
                Exception(
                    "Pairing code tidak valid"
                )
            )
        }

        val caregiverDocument =
            caregiverQuery.documents.first()

        val caregiverUid =
            caregiverDocument.id

        val authResult =
            auth.createUserWithEmailAndPassword(
                email,
                password
            ).await()

        val uid =
            authResult.user?.uid ?: ""

        val user = User(
            uid = uid,
            name = name,
            email = email,
            role = "family",
            caregiverUid = caregiverUid
        )

        firestore.collection("users")
            .document(uid)
            .set(user)
            .await()

        return Result.success(
            "Family berhasil terhubung"
        )
    }

    private fun generatePairingCode(): String {

        val number = (100..999).random()
        val letter = ('A'..'Z').random()

        return "CM-$number$letter"
    }

    suspend fun login(
        email: String,
        password: String
    ): Result<String> {

        return try {

            val authResult =
                auth.signInWithEmailAndPassword(
                    email,
                    password
                ).await()

            val uid =
                authResult.user?.uid ?: ""

            val document =
                firestore.collection("users")
                    .document(uid)
                    .get()
                    .await()

            val role =
                document.getString("role") ?: ""

            Result.success(role)

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    suspend fun getCurrentUser(): Result<User> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("User not found"))
            val document = firestore.collection("users").document(uid).get().await()
            val user = document.toObject(User::class.java) ?: return Result.failure(Exception("Profile not found"))
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }

    suspend fun getLinkedCaregiver(): Result<User> {

        return try {

            val familyUid =
                auth.currentUser?.uid
                    ?: return Result.failure(
                        Exception("User not found")
                    )

            val familyDocument =
                firestore.collection("users")
                    .document(familyUid)
                    .get()
                    .await()

            val caregiverUid =
                familyDocument.getString(
                    "caregiverUid"
                )
                    ?: return Result.failure(
                        Exception("Caregiver not linked")
                    )

            val caregiverDocument =
                firestore.collection("users")
                    .document(caregiverUid)
                    .get()
                    .await()

            val caregiver =
                caregiverDocument.toObject(
                    User::class.java
                )
                    ?: return Result.failure(
                        Exception("Caregiver not found")
                    )

            Result.success(caregiver)

        } catch (e: Exception) {

    suspend fun updateUserProfile(jobTitle: String, age: Int, bio: String, experience: List<String>, skills: List<String>): Result<String> {
        return try {
            val uid = auth.currentUser?.uid ?: throw Exception("User not logged in")
            val updates = mapOf(
                "jobTitle" to jobTitle,
                "age" to age,
                "bio" to bio,
                "experience" to experience,
                "skills" to skills
            )
            firestore.collection("users").document(uid).update(updates).await()
            Result.success("Profil berhasil diperbarui")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun requestJoinFamily(targetCode: String): Result<String> {
        return try {
            val uid = auth.currentUser?.uid ?: throw Exception("User not logged in")
            // Cari Pasien yang memiliki kode ini
            val snapshot = firestore.collection("users")
                .whereEqualTo("pairingCode", targetCode)
                .get()
                .await()
                
            if (snapshot.isEmpty) throw Exception("Kode keluarga tidak valid")
            val patientDoc = snapshot.documents[0]
            val patientUid = patientDoc.getString("uid") ?: throw Exception("Data pasien tidak valid")
            
            // Ambil data caregiver
            val caregiverDoc = firestore.collection("users").document(uid).get().await()
            val caregiverName = caregiverDoc.getString("name") ?: "Caregiver"
            
            // Buat request
            val requestRef = firestore.collection("family_requests").document()
            val request = com.mdp.caremate.data.model.FamilyRequest(
                requestId = requestRef.id,
                targetCode = targetCode,
                patientUid = patientUid,
                caregiverUid = uid,
                caregiverName = caregiverName,
                status = "pending"
            )
            
            requestRef.set(request).await()
            Result.success("Permintaan bergabung berhasil dikirim")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun acceptJoinRequest(requestId: String, caregiverUid: String): Result<String> {
        return try {
            val patientUid = auth.currentUser?.uid ?: throw Exception("User not logged in")
            
            // 1. Update status request jadi accepted
            firestore.collection("family_requests").document(requestId)
                .update("status", "accepted").await()
                
            // 2. Update caregiver data agar terhubung dengan pasien
            firestore.collection("users").document(caregiverUid)
                .update("connectedPatientUid", patientUid).await()
                
            Result.success("Berhasil menerima caregiver")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun rejectJoinRequest(requestId: String): Result<String> {
        return try {
            firestore.collection("family_requests").document(requestId)
                .update("status", "rejected").await()
            Result.success("Permintaan ditolak")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}