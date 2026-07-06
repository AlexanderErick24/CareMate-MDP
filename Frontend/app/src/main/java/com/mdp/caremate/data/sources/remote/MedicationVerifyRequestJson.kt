package com.mdp.caremate.data.sources.remote

import com.google.gson.annotations.SerializedName

data class MedicationVerifyRequestJson(
    @SerializedName("imageBase64")
    val imageBase64: String,
    
    @SerializedName("expectedMedication")
    val expectedMedication: String
)
