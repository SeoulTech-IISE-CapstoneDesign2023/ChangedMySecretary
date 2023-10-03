package com.example.fastcampus.part3.design.util

import com.example.fastcampus.part3.design.model.location.SearchLocationService
import com.example.fastcampus.part3.design.model.car.SearchCarService
import com.example.fastcampus.part3.design.model.route.SearchRouteService
import com.example.fastcampus.part3.design.model.route.bus.realLocation.SearchBusRealLocationService
import com.example.fastcampus.part3.design.model.route.bus.realtime.SearchBusRealTimeService
import com.example.fastcampus.part3.design.model.route.subway.SearchSubwayTimeTableService
import com.example.fastcampus.part3.design.model.walk.SearchWalkService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object LocationRetrofitManager {

    private const val BASE_URL = "https://apis.openapi.sk.com/"
    private const val BASE_URL_ODSAY = "https://api.odsay.com/v1/api/"
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor {
            val request = it.request().newBuilder()
                .addHeader("accept", "application/json")
                .addHeader("appKey", "IZF9Qw0f7k2uAhARttjL76jEdYrQefbt5iJPZMZ8")
                .build()
            it.proceed(request)
        }
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val retrofitOdsay: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL_ODSAY)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val searchLocationService: SearchLocationService by lazy { retrofit.create(SearchLocationService::class.java) }

    val searchWalkService: SearchWalkService by lazy { retrofit.create(SearchWalkService::class.java) }

    val searchCarService: SearchCarService by lazy { retrofit.create(SearchCarService::class.java) }

    val searchRootService: SearchRouteService by lazy { retrofitOdsay.create(SearchRouteService::class.java) }

    val searchSubwayTimeTableService: SearchSubwayTimeTableService by lazy {
        retrofitOdsay.create(
            SearchSubwayTimeTableService::class.java
        )
    }

    val searchBusRealLocationService: SearchBusRealLocationService by lazy {
        retrofitOdsay.create(
            SearchBusRealLocationService::class.java
        )
    }

    val searchBusRealTimeService: SearchBusRealTimeService by lazy {
        retrofitOdsay.create(
            SearchBusRealTimeService::class.java
        )
    }
}