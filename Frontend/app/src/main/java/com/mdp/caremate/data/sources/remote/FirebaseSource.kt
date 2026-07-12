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
import com.mdp.caremate.data.model.QuitRequest
import com.google.firebase.firestore.FieldValue

class FirebaseSource {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // USER

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

    // EVENT

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

    suspend fun saveEvent(event: Event): Boolean {
        return try {
            firestore.collection("events")
                .document(event.eid) // Menggunakan ID yang sudah dibuat di ViewModel
                .set(event)
                .await()
            true // Berhasil
        } catch (e: Exception) {
            e.printStackTrace()
            false // Gagal
        }
    }

    suspend fun deleteEvent(eventId: String): Boolean {
        return try {
            firestore.collection("events")
                .document(eventId)
                .delete()
                .await()
            true // Berhasil menghapus
        } catch (e: Exception) {
            e.printStackTrace()
            false // Gagal menghapus
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

    suspend fun updateConnectedPatientUid(patientUid: String): Result<Boolean> {
        return try {
            val uid = auth.currentUser?.uid ?: throw Exception("User not logged in")
            firestore.collection("users").document(uid)
                .update("connectedPatientUid", patientUid).await()
            Result.success(true)
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
                    message = message,
                    timestamp = System.currentTimeMillis()
                )

            // Save the message to the messages subcollection
            firestore.collection("chatRooms")
                .document(pairingCode)
                .collection("messages")
                .add(chatMessage)
                .await()

            // Update the chatRoom document:
            // - Record who sent the last message (for badge logic)
            // - Increment unreadCount by 1
            firestore.collection("chatRooms")
                .document(pairingCode)
                .update(
                    mapOf(
                        "lastSenderId" to currentUser.uid,
                        "unreadCount" to FieldValue.increment(1)
                    )
                )
                .await()

            Result.success(Unit)

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

// ============================================================
// 2. ADD this new function (observe the chatRoom document for badge)
// ============================================================

    fun observeChatRoom(
        pairingCode: String,
        onChanged: (ChatRoom) -> Unit
    ): ListenerRegistration {

        return firestore.collection("chatRooms")
            .document(pairingCode)
            .addSnapshotListener { snapshot, _ ->

                if (snapshot == null) return@addSnapshotListener

                val room =
                    snapshot.toObject(ChatRoom::class.java)
                        ?: return@addSnapshotListener

                onChanged(room)
            }
    }

// ============================================================
// 3. ADD this new function (reset unreadCount when user opens chat)
// ============================================================

    suspend fun markChatAsRead(pairingCode: String): Result<Unit> {

        return try {

            firestore.collection("chatRooms")
                .document(pairingCode)
                .update("unreadCount", 0)
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

    suspend fun updateUsername(newName: String): Result<String> {
        return try {
            val uid = auth.currentUser?.uid
                ?: throw Exception("User not logged in")
            firestore.collection("users")
                .document(uid)
                .update("name", newName)
                .await()
            Result.success("Nama berhasil diperbarui")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun changePassword(
        currentPassword: String,
        newPassword: String
    ): Result<String> {
        return try {
            val user = auth.currentUser
                ?: throw Exception("User not logged in")

            // Firebase requires re-authentication before changing password
            val email = user.email
                ?: throw Exception("Email tidak ditemukan")

            val credential = com.google.firebase.auth.EmailAuthProvider
                .getCredential(email, currentPassword)

            user.reauthenticate(credential).await()
            user.updatePassword(newPassword).await()

            Result.success("Password berhasil diubah")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadProfilePhoto(photoBase64: String): Result<String> {
        return try {
            val uid = auth.currentUser?.uid
                ?: throw Exception("User not logged in")

            // Store the base64 string directly in Firestore
            // (same approach as medication photo in DashboardFragment)
            firestore.collection("users")
                .document(uid)
                .update("photoUrl", photoBase64)
                .await()

            Result.success(photoBase64)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==================================================
    // QUIT SYSTEM
    // ==================================================

    // Caregiver sends a quit request to all linked family members
    suspend fun sendCaregiverQuitRequest(): Result<String> {

        return try {

            val caregiverUid =
                auth.currentUser?.uid
                    ?: return Result.failure(
                        Exception("User not found")
                    )

            // Check if a pending request already exists to avoid duplicates
            val existing =
                firestore.collection("quit_requests")
                    .whereEqualTo("caregiverUid", caregiverUid)
                    .whereEqualTo("type", "caregiver_quit")
                    .whereEqualTo("status", "pending")
                    .get()
                    .await()

            if (!existing.isEmpty) {
                return Result.failure(
                    Exception("ALREADY_PENDING")
                )
            }

            val requestId =
                firestore.collection("quit_requests")
                    .document()
                    .id

            val request = QuitRequest(
                requestId = requestId,
                type = "caregiver_quit",
                status = "pending",
                caregiverUid = caregiverUid,
                createdAt = System.currentTimeMillis()
            )

            firestore.collection("quit_requests")
                .document(requestId)
                .set(request)
                .await()

            Result.success(requestId)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Family member sends a quit request to the caregiver
    suspend fun sendFamilyQuitRequest(): Result<String> {

        return try {

            val familyUid =
                auth.currentUser?.uid
                    ?: return Result.failure(
                        Exception("User not found")
                    )

            val familyDoc =
                firestore.collection("users")
                    .document(familyUid)
                    .get()
                    .await()

            val caregiverUid =
                familyDoc.getString("caregiverUid")
                    ?: return Result.failure(
                        Exception("No caregiver linked")
                    )

            // Check if a pending request already exists
            val existing =
                firestore.collection("quit_requests")
                    .whereEqualTo("familyUid", familyUid)
                    .whereEqualTo("type", "family_quit")
                    .whereEqualTo("status", "pending")
                    .get()
                    .await()

            if (!existing.isEmpty) {
                return Result.failure(
                    Exception("ALREADY_PENDING")
                )
            }

            val requestId =
                firestore.collection("quit_requests")
                    .document()
                    .id

            val request = QuitRequest(
                requestId = requestId,
                type = "family_quit",
                status = "pending",
                caregiverUid = caregiverUid,
                familyUid = familyUid,
                createdAt = System.currentTimeMillis()
            )

            firestore.collection("quit_requests")
                .document(requestId)
                .set(request)
                .await()

            Result.success(requestId)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get a pending caregiver_quit request for the current caregiver
    // Used by ProfileFragment to show "Waiting for approval" status
    suspend fun getPendingCaregiverQuitRequest(): Result<QuitRequest?> {

        return try {

            val caregiverUid =
                auth.currentUser?.uid
                    ?: return Result.failure(
                        Exception("User not found")
                    )

            val snapshot =
                firestore.collection("quit_requests")
                    .whereEqualTo("caregiverUid", caregiverUid)
                    .whereEqualTo("type", "caregiver_quit")
                    .get()
                    .await()

            // Return the most recent non-dismissed request
            val request =
                snapshot.documents
                    .mapNotNull { it.toObject(QuitRequest::class.java) }
                    .filter { it.status == "pending" || it.status == "rejected" }
                    .maxByOrNull { it.createdAt }

            Result.success(request)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get a pending caregiver_quit request visible to the current family member
    // Used by FamilyManagementFragment to show the approval card
    suspend fun getPendingCaregiverQuitRequestForFamily(): Result<QuitRequest?> {

        return try {

            val familyUid =
                auth.currentUser?.uid
                    ?: return Result.failure(
                        Exception("User not found")
                    )

            val familyDoc =
                firestore.collection("users")
                    .document(familyUid)
                    .get()
                    .await()

            val caregiverUid =
                familyDoc.getString("caregiverUid")
                    ?: return Result.success(null)

            val snapshot =
                firestore.collection("quit_requests")
                    .whereEqualTo("caregiverUid", caregiverUid)
                    .whereEqualTo("type", "caregiver_quit")
                    .whereEqualTo("status", "pending")
                    .get()
                    .await()

            val request =
                snapshot.documents
                    .mapNotNull { it.toObject(QuitRequest::class.java) }
                    .maxByOrNull { it.createdAt }

            Result.success(request)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get a pending family_quit request visible to the current caregiver
    // Used by ProfileFragment to show the family quit approval card
    suspend fun getPendingFamilyQuitRequestForCaregiver(): Result<QuitRequest?> {

        return try {

            val caregiverUid =
                auth.currentUser?.uid
                    ?: return Result.failure(
                        Exception("User not found")
                    )

            val snapshot =
                firestore.collection("quit_requests")
                    .whereEqualTo("caregiverUid", caregiverUid)
                    .whereEqualTo("type", "family_quit")
                    .whereEqualTo("status", "pending")
                    .get()
                    .await()

            val request =
                snapshot.documents
                    .mapNotNull { it.toObject(QuitRequest::class.java) }
                    .maxByOrNull { it.createdAt }

            Result.success(request)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get pending family_quit request for the current family member
    // Used by FamilyManagementFragment to show "Waiting for approval" per member
    suspend fun getPendingFamilyQuitRequestForSelf(): Result<QuitRequest?> {

        return try {

            val familyUid =
                auth.currentUser?.uid
                    ?: return Result.failure(
                        Exception("User not found")
                    )

            val snapshot =
                firestore.collection("quit_requests")
                    .whereEqualTo("familyUid", familyUid)
                    .whereEqualTo("type", "family_quit")
                    .get()
                    .await()

            val request =
                snapshot.documents
                    .mapNotNull { it.toObject(QuitRequest::class.java) }
                    .filter { it.status == "pending" || it.status == "rejected" }
                    .maxByOrNull { it.createdAt }

            Result.success(request)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Family member approves the caregiver_quit request
    // This disconnects ALL family members from the caregiver and resets caregiver patient data
    suspend fun approveCaregiverQuit(
        requestId: String,
        newPatientName: String,
        newPairingCode: String
    ): Result<Unit> {

        return try {

            val requestDoc =
                firestore.collection("quit_requests")
                    .document(requestId)
                    .get()
                    .await()

            val caregiverUid =
                requestDoc.getString("caregiverUid")
                    ?: return Result.failure(
                        Exception("Request not found")
                    )

            // 1. Mark request as approved
            firestore.collection("quit_requests")
                .document(requestId)
                .update("status", "approved")
                .await()

            // 2. Find all family members linked to this caregiver and clear their caregiverUid
            val familySnapshot =
                firestore.collection("users")
                    .whereEqualTo("caregiverUid", caregiverUid)
                    .get()
                    .await()

            for (doc in familySnapshot.documents) {
                firestore.collection("users")
                    .document(doc.id)
                    .update("caregiverUid", "")
                    .await()
            }

            // 3. Reset caregiver: new patient name + new pairing code
            firestore.collection("users")
                .document(caregiverUid)
                .update(
                    mapOf(
                        "patientName" to newPatientName,
                        "pairingCode" to newPairingCode
                    )
                )
                .await()

            // 4. Create a new chat room for the new pairing code
            createChatRoom(
                newPairingCode,
                caregiverUid,
                newPatientName
            )

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Family member rejects the caregiver_quit request
    suspend fun rejectCaregiverQuit(
        requestId: String,
        reason: String
    ): Result<Unit> {

        return try {

            firestore.collection("quit_requests")
                .document(requestId)
                .update(
                    mapOf(
                        "status" to "rejected",
                        "reason" to reason
                    )
                )
                .await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Caregiver approves a family_quit request
    // Only disconnects that specific family member
    suspend fun approveFamilyQuit(requestId: String): Result<Unit> {

        return try {

            val requestDoc =
                firestore.collection("quit_requests")
                    .document(requestId)
                    .get()
                    .await()

            val familyUid =
                requestDoc.getString("familyUid")
                    ?: return Result.failure(
                        Exception("Request not found")
                    )

            // 1. Mark request as approved
            firestore.collection("quit_requests")
                .document(requestId)
                .update("status", "approved")
                .await()

            // 2. Clear caregiverUid only for this specific family member
            firestore.collection("users")
                .document(familyUid)
                .update("caregiverUid", "")
                .await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Caregiver rejects a family_quit request
    suspend fun rejectFamilyQuit(
        requestId: String,
        reason: String
    ): Result<Unit> {

        return try {

            firestore.collection("quit_requests")
                .document(requestId)
                .update(
                    mapOf(
                        "status" to "rejected",
                        "reason" to reason
                    )
                )
                .await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Family member reconnects to a new caregiver using a pairing code
    // Called after a family_quit is approved — no new account is created
    suspend fun reconnectFamilyToNewCaregiver(newPairingCode: String): Result<Unit> {

        return try {

            val familyUid =
                auth.currentUser?.uid
                    ?: return Result.failure(
                        Exception("User not found")
                    )

            // Look up the caregiver with the given pairing code
            val caregiverQuery =
                firestore.collection("users")
                    .whereEqualTo("pairingCode", newPairingCode)
                    .whereEqualTo("role", "caregiver")
                    .get()
                    .await()

            if (caregiverQuery.isEmpty) {
                return Result.failure(
                    Exception("Invalid Pairing Code")
                )
            }

            val newCaregiverUid =
                caregiverQuery.documents.first().id

            // Update this family member's caregiverUid to point to the new caregiver
            firestore.collection("users")
                .document(familyUid)
                .update("caregiverUid", newCaregiverUid)
                .await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Dismiss a quit request (used after caregiver sees rejection reason)
    suspend fun dismissQuitRequest(requestId: String): Result<Unit> {

        return try {

            firestore.collection("quit_requests")
                .document(requestId)
                .delete()
                .await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePatientName(newPatientName: String): Result<String> {
        return try {
            val uid = auth.currentUser?.uid
                ?: throw Exception("User not logged in")

            firestore.collection("users")
                .document(uid)
                .update("patientName", newPatientName)
                .await()

            Result.success("Nama pasien berhasil diperbarui")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}