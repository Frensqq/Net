package com.example.networklibrary.data.remoute

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.networklibrary.data.remote.PBApi

object PBApiServis {
    // ✅ Base URL должен заканчиваться на /api/
    private const val BASE_URL = "http://10.0.2.2:8090/api/"

    val instance: PBApi by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(PBApi::class.java)
    }
}