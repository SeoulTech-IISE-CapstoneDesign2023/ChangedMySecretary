package com.example.fastcampus.part3.design.model.route.bus.realLocation

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface SearchBusRealLocationService {
    @GET("realtimeRoute")
    fun getRouteId(
        @Query("apiKey") apiKey: String,
        @Query("busID") busId: Int
    ): Call<BusRealTimeLocationDTO>
}