package com.mdp.caremate.data.sources.remote

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestore
import com.mdp.caremate.data.model.ChatRoom
import com.mdp.caremate.data.model.User
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.ListenerRegistration
import com.mdp.caremate.data.model.ChatMessage
import com.mdp.caremate.data.model.FamilyMember
import com.mdp.caremate.data.model.Event

class FirebaseSource {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun getAllUser(): List<User> {
        return try {
            firestore.collection("users")
                .get()
                .await()
                .toObjects(User::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getUserByID(id: String): User? {
        return try {
            firestore.collection("users")
                .document(id)
                .get()
                .await()
                .toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getAllEvent(): List<Event> {
        return try {
            firestore.collection("events")
                .get()
                .await()
                .toObjects(Event::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

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

        createChatRoom(
            generatedCode,
            uid,
            patientName
        )

        return Result.success(generatedCode)
    }

    private suspend fun registerFamily(
        name: String,
        email: String,
        password: String,
        pairingCode: String
    ): Result<String> {

        return try {

            val caregiverQuery =
                firestore.collection("users")
                    .whereEqualTo(
                        "pairingCode",
                        pairingCode
                    )
                    .whereEqualTo(
                        "role",
                        "caregiver"
                    )
                    .get()
                    .await()

            if (
                caregiverQuery.isEmpty
            ) {

                return Result.failure(
                    Exception(
                        "Invalid Pairing Code"
                    )
                )
            }

            val caregiverDoc =
                caregiverQuery.documents.first()

            val caregiverUid =
                caregiverDoc.id

            // =========================
            // PREMIUM VALIDATION
            // =========================

            val familyCount =
                firestore.collection("users")
                    .whereEqualTo(
                        "caregiverUid",
                        caregiverUid
                    )
                    .get()
                    .await()
                    .size()

            if (
                familyCount >= 2
            ) {

                return Result.failure(
                    Exception(
                        "PREMIUM_REQUIRED"
                    )
                )
            }

            val authResult =
                auth.createUserWithEmailAndPassword(
                    email,
                    password
                ).await()

            val uid =
                authResult.user?.uid ?: ""

            val user =
                User(
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

            Result.success(
                "Register berhasil"
            )

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

            Log.d("LOGIN", "Email = $email")

            val authResult =
                auth.signInWithEmailAndPassword(
                    email,
                    password
                ).await()

            Log.d("LOGIN", "Firebase Auth SUCCESS")

            val uid =
                authResult.user?.uid ?: ""

            Log.d("LOGIN", "UID = $uid")

            val document =
                firestore.collection("users")
                    .document(uid)
                    .get()
                    .await()

            Log.d("LOGIN", "Firestore SUCCESS")


            val role =
                document.getString("role") ?: ""

            Log.d("LOGIN", "ROLE = $role")

            Result.success(role)

        } catch (e: Exception) {
            Log.e(
                "LOGIN_ERROR",
                e.javaClass.simpleName,
                e
            )
            Result.failure(e)
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Log.e("LOGIN", "Invalid email/password")
            return Result.failure(e)
        }

        catch (e: FirebaseAuthInvalidUserException) {
            Log.e("LOGIN", "User does not exist")
            return Result.failure(e)
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

    suspend fun updateUserProfile(name: String, jobTitle: String, age: Int, bio: String, experience: List<String>, skills: List<String>): Result<String> {
        return try {
            val uid = auth.currentUser?.uid ?: throw Exception("User not logged in")
            val updates = mapOf(
                "name" to name,
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

    suspend fun updateProfilePhoto(photoUrl: String): Result<String> {
        return try {
            val uid = auth.currentUser?.uid ?: throw Exception("User not logged in")
            firestore.collection("users").document(uid).update("photoUrl", photoUrl).await()
            Result.success("Foto profil berhasil diperbarui")
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

    // CHAT
    suspend fun createChatRoom(
        pairingCode: String,
        caregiverUid: String,
        patientName: String
    ) {

        val room = ChatRoom(
            pairingCode = pairingCode,
            caregiverUid = caregiverUid,
            patientName = patientName
        )

        firestore.collection("chatRooms")
            .document(pairingCode)
            .set(room)
            .await()
    }

    suspend fun sendMessage(
        pairingCode: String,
        message: String
    ): Result<Unit> {

        return try {

            val currentUser =
                getCurrentUser().getOrNull()
                    ?: return Result.failure(
                        Exception("User not found")
                    )

            val chatMessage =
                ChatMessage(
                    senderId = currentUser.uid,
                    senderName = currentUser.name,
                    message = message
                )

            firestore.collection("chatRooms")
                .document(pairingCode)
                .collection("messages")
                .add(chatMessage)
                .await()

            Result.success(Unit)

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    fun observeMessages(

        pairingCode: String,

        onMessagesChanged: (List<ChatMessage>) -> Unit

    ): ListenerRegistration {

        return firestore.collection("chatRooms")
            .document(pairingCode)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, _ ->

                if (snapshot == null) return@addSnapshotListener

                val messages =
                    snapshot.toObjects(
                        ChatMessage::class.java
                    )

                onMessagesChanged(
                    messages
                )
            }
    }

    suspend fun getPairingCodeForCurrentUser():
            Result<String> {

        return try {

            val uid =
                auth.currentUser?.uid
                    ?: return Result.failure(
                        Exception(
                            "User not found"
                        )
                    )

            val currentUserDoc =
                firestore.collection("users")
                    .document(uid)
                    .get()
                    .await()

            val role =
                currentUserDoc.getString(
                    "role"
                ) ?: ""

            if (role == "caregiver") {

                val pairingCode =
                    currentUserDoc.getString(
                        "pairingCode"
                    ) ?: ""

                return Result.success(
                    pairingCode
                )
            }

            if (role == "family") {

                val caregiverUid =
                    currentUserDoc.getString(
                        "caregiverUid"
                    ) ?: ""

                val caregiverDoc =
                    firestore.collection("users")
                        .document(caregiverUid)
                        .get()
                        .await()

                val pairingCode =
                    caregiverDoc.getString(
                        "pairingCode"
                    ) ?: ""

                return Result.success(
                    pairingCode
                )
            }

            Result.failure(
                Exception("Invalid role")
            )

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    suspend fun getFamilyMembers():
            Result<List<String>> {

        return try {

            val currentUid =
                auth.currentUser?.uid
                    ?: return Result.failure(
                        Exception("User not found")
                    )

            val currentUser =
                firestore.collection("users")
                    .document(currentUid)
                    .get()
                    .await()

            val role =
                currentUser.getString("role")
                    ?: ""

            val caregiverUid =

                if (role == "caregiver") {

                    currentUid

                } else {

                    currentUser.getString(
                        "caregiverUid"
                    ) ?: ""
                }

            val snapshot =
                firestore.collection("users")
                    .whereEqualTo(
                        "caregiverUid",
                        caregiverUid
                    )
                    .get()
                    .await()

            val names =
                snapshot.documents.mapNotNull {

                    it.getString("name")
                }

            Result.success(names)

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    suspend fun getFamilyMemberList():
            Result<List<FamilyMember>> {

        return try {

            val currentUid =
                auth.currentUser?.uid ?: ""

            val currentUser =
                firestore.collection("users")
                    .document(currentUid)
                    .get()
                    .await()

            val role =
                currentUser.getString("role")
                    ?: ""

            val caregiverUid =

                if(role == "caregiver") {

                    currentUid

                } else {

                    currentUser.getString(
                        "caregiverUid"
                    ) ?: ""
                }

            val snapshot =
                firestore.collection("users")
                    .whereEqualTo(
                        "caregiverUid",
                        caregiverUid
                    )
                    .get()
                    .await()

            val familyMembers =

                snapshot.documents.map {

                    FamilyMember(

                        uid = it.id,

                        name =
                            it.getString("name")
                                ?: "",

                        email =
                            it.getString("email")
                                ?: ""
                    )
                }

            Result.success(
                familyMembers
            )

        } catch(e: Exception) {

            Result.failure(e)
        }
    }

    suspend fun saveAiAlert(alert: com.mdp.caremate.data.model.AiAlert): Result<Unit> {
        return try {
            firestore.collection("ai_alerts")
                .document(alert.id)
                .set(alert)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAiAlertsForFamily(): Result<List<com.mdp.caremate.data.model.AiAlert>> {
        return try {
            val currentUid = auth.currentUser?.uid ?: return Result.failure(Exception("Not logged in"))
            val currentUser = firestore.collection("users").document(currentUid).get().await()
            val role = currentUser.getString("role") ?: ""
            val caregiverUid = if (role == "caregiver") {
                currentUid
            } else {
                currentUser.getString("caregiverUid") ?: ""
            }

            val snapshot = firestore.collection("ai_alerts")
                .whereEqualTo("caregiverId", caregiverUid)
                .get()
                .await()

            val alerts = snapshot.documents.mapNotNull {
                it.toObject(com.mdp.caremate.data.model.AiAlert::class.java)
            }.sortedByDescending { it.timestamp }
            
            Result.success(alerts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}