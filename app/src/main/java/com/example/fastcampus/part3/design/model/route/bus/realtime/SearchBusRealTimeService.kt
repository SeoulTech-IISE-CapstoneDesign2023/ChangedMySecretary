package com.example.fastcampus.part3.design.model.route.bus.realtime

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface SearchBusRealTimeService {
    @GET("realtimeStation")
    fun getBusRealTime(
        @Query("apiKey") apikey: String,
        @Query("stationID") stationId: Int,
    ): Call<RealTimeArrivalBus>
}