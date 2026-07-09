package com.mdp.caremate.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PatientMedicalProfile(
    val diagnosis: String,
    val allergies: String,
    val texture: String,
    val preferences: String
)

@JsonClass(generateAdapter = true)
data class RecipeIngredient(
    val name: String,
    val amount: String
)

@JsonClass(generateAdapter = true)
data class Recipe(
    val id: String,
    val title: String,
    val imageSearchKeyword: String,
    val estTimeMin: Int,
    val portions: Int? = 1,
    val medicalRationale: String,
    val safetyBadge: String,
    val ingredients: List<RecipeIngredient>,
    val steps: List<String>,
    val youtubeQuery: String
)

@JsonClass(generateAdapter = true)
data class GenerateRecipeRequest(
    val patientProfile: PatientMedicalProfile,
    val mealType: String,
    val ingredients: String?
)

@JsonClass(generateAdapter = true)
data class GenerateRecipeResponse(
    val message: String,
    val data: List<Recipe>
)

