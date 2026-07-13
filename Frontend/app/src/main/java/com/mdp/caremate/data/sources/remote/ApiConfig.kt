package com.mdp.caremate.data.sources.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object ApiConfig {
    // Production Render URL (enforces HTTPS)
    const val BASE_URL = "https://caremate-backend.onrender.com/"
    
    // For local debugging, change to: "http://10.0.2.2:3000/"
    // const val BASE_URL = "http://10.0.2.2:3000/"

    private val okHttpClient: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            // Only log headers in debug builds, completely disabled in release builds
            level = if (com.mdp.caremate.BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.HEADERS
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
        OkHttpClient.Builder()
            .connectTimeout(90, TimeUnit.SECONDS)
            .readTimeout(90, TimeUnit.SECONDS)
            .writeTimeout(90, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    private val gsonRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val moshiRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }

    // Thread-safe Lazy Singleton instance for general WebService (Gson)
    val webService: WebService by lazy {
        gsonRetrofit.create(WebService::class.java)
    }

    // Thread-safe Lazy Singleton instance for SmartNutritionApi (Moshi)
    val smartNutritionApi: SmartNutritionApi by lazy {
        moshiRetrofit.create(SmartNutritionApi::class.java)
    }

    // Compatibility getter method for legacy calls
    fun getWebService(): WebService = webService
}