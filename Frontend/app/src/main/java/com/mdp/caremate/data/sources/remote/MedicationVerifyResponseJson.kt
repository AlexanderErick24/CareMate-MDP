package com.mdp.caremate.data.sources.remote

import com.google.gson.annotations.SerializedName

data class MedicationVerifyResponseJson(
    @SerializedName("isValid")
    val isValid: Boolean,
    
    @SerializedName("severity")
    val severity: String,
    
    @SerializedName("title")
    val title: String,
    
    @SerializedName("description")
    val description: String,
    
    @SerializedName("timestamp")
    val timestamp: Long
)
