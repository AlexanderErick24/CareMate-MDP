package com.mdp.caremate.data.repositories
import com.mdp.caremate.data.model.Event
import com.mdp.caremate.data.model.User

interface AdminRepository {
    suspend fun getAllUser():List<User>
    suspend fun getAllEvent():List<Event>
    suspend fun getUserByID(id:String):User?
}