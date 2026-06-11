package com.mdp.caremate.data.model

import com.google.gson.annotations.SerializedName

data class Event(
    @SerializedName("id")
    val eid: String = "",
    
    @SerializedName("title")
    val name: String = "",
    
    val date: String = "",
    val time: String = "",
    
    @SerializedName("location")
    val place: String = "",
    
    @SerializedName("slots")
    val capacity: String = "",
    
    val listed: Boolean = false
) : java.io.Serializable
