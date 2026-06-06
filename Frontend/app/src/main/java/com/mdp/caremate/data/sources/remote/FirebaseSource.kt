package com.mdp.caremate.data.sources.remote

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mdp.caremate.data.model.User
import kotlinx.coroutines.tasks.await


class FirebaseSource {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun registerCaregiver(
        name: String,
        email: String,
        password: String
    ): Result<String> {

        return try {

            val authResult =
                auth.createUserWithEmailAndPassword(email, password).await()

            val uid = authResult.user?.uid ?: ""

            val pairingCode = generatePairingCode()

            val user = User(
                uid = uid,
                name = name,
                email = email,
                role = "caregiver",
                pairingCode = pairingCode
            )

            firestore.collection("users")
                .document(uid)
                .set(user)
//                .await()

            Result.success("Register berhasil")

        } catch (e: Exception) {
            Result.failure(e)
        }
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
}