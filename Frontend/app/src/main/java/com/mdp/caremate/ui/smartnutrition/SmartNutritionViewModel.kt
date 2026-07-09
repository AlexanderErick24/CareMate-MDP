package com.mdp.caremate.ui.smartnutrition

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mdp.caremate.data.model.GenerateRecipeRequest
import com.mdp.caremate.data.model.PatientMedicalProfile
import com.mdp.caremate.data.model.Recipe
import com.mdp.caremate.data.sources.remote.SmartNutritionApi
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

sealed class SmartNutritionState {
    object Idle : SmartNutritionState()
    object Loading : SmartNutritionState()
    data class Success(val recipes: List<Recipe>) : SmartNutritionState()
    data class Error(val message: String) : SmartNutritionState()
}

class SmartNutritionViewModel : ViewModel() {

    private val _uiState = MutableLiveData<SmartNutritionState>(SmartNutritionState.Idle)
    val uiState: LiveData<SmartNutritionState> = _uiState

    // Menyimpan resep yang dipilih untuk ditampilkan di halaman detail
    var selectedRecipe: Recipe? = null

    // In a real app with DI (Hilt/Dagger), this would be injected.
    // For this prototype, we'll instantiate Retrofit here.
    private val api: SmartNutritionApi by lazy {
        val interceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .connectTimeout(45, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(45, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(45, java.util.concurrent.TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:3000/") // Localhost for Android Emulator
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(SmartNutritionApi::class.java)
    }

    fun generateRecipes(
        patientProfile: PatientMedicalProfile,
        mealType: String,
        ingredients: String?
    ) {
        _uiState.value = SmartNutritionState.Loading
        viewModelScope.launch {
            try {
                val request = GenerateRecipeRequest(
                    patientProfile = patientProfile,
                    mealType = mealType,
                    ingredients = ingredients
                )
                val response = api.generateRecipes(request)
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = SmartNutritionState.Success(response.body()!!.data)
                } else {
                    _uiState.value = SmartNutritionState.Error("Gagal mengambil resep: \${response.message()}")
                }
            } catch (e: Exception) {
                _uiState.value = SmartNutritionState.Error("Error: ${e.message}")
            }
        }
    }
}
