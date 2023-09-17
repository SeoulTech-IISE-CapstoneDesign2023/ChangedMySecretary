package com.example.fastcampus.part3.design

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

object LocationRetrofitManager {

    private const val BASE_URL = "https://apis.openapi.sk.com/"
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor {
            val request = it.request().newBuilder()
                .addHeader("accept", "application/json")
                .addHeader("appKey", "IZF9Qw0f7k2uAhARttjL76jEdYrQefbt5iJPZMZ8")
                .build()
            it.proceed(request)
        }
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val searchLocationService : SearchLocationService by lazy { retrofit.create(SearchLocationService::class.java) }
}