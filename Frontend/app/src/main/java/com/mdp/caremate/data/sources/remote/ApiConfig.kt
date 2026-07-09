package com.mdp.caremate.data.sources.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object ApiConfig {
    const val BASE_URL = "http://10.0.2.2:3000/"
    fun getWebService(): WebService {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(WebService::class.java)
    }
}