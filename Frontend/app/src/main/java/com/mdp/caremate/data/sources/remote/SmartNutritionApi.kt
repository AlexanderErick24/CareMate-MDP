package com.mdp.caremate.data.sources.remote

import com.mdp.caremate.data.model.GenerateRecipeRequest
import com.mdp.caremate.data.model.GenerateRecipeResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface SmartNutritionApi {
    @POST("/api/smart-nutrition/generate")
    suspend fun generateRecipes(
        @Body request: GenerateRecipeRequest
    ): Response<GenerateRecipeResponse>
}
