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

class SmartNutritionViewModel(
    // Parameter opsional: jika tidak diberikan (runtime normal), gunakan Retrofit default.
    // Jika diberikan (saat testing), gunakan mock API.
    private val apiOverride: SmartNutritionApi? = null
) : ViewModel() {

    private val _uiState = MutableLiveData<SmartNutritionState>(SmartNutritionState.Idle)
    val uiState: LiveData<SmartNutritionState> = _uiState

    // Menyimpan resep yang dipilih untuk ditampilkan di halaman detail
    var selectedRecipe: Recipe? = null

    // Gunakan apiOverride jika ada (testing), jika tidak gunakan centralized singleton (runtime)
    private val api: SmartNutritionApi get() = apiOverride ?: com.mdp.caremate.data.sources.remote.ApiConfig.smartNutritionApi

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

    fun resetState() {
        if (_uiState.value is SmartNutritionState.Error) {
            _uiState.value = SmartNutritionState.Idle
        }
    }
}
