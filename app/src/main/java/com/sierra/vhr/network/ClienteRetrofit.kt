package com.sierra.vhr.network

import com.sierra.vhr.network.api.VhrApi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ClienteRetrofit {

    // Ip del backend
    private const val URL = "http://192.168.0.160:9090/"

    private val interceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val cliente = OkHttpClient.Builder()
        .addInterceptor(interceptor)
        .build()

    val api: VhrApi by lazy {
        Retrofit.Builder()
            .baseUrl(URL)
            .client(cliente)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(VhrApi::class.java)
    }
}