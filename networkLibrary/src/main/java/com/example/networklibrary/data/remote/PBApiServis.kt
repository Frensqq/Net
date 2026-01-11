package com.example.networklibrary.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object PBApiServis {

    private const val BASE_URL = "http://10.0.2.2:8090/api/"

    val instance: PBApi by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(PBApi::class.java)
    }

    fun getImageUrl(collectionId: String, recordId: String, fileName: String): String {
        return "$BASE_URL"+"files/$collectionId/$recordId/$fileName"
    }
}