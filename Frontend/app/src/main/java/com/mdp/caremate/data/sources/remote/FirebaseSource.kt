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

            Result.failure(e)
        }
    }
}