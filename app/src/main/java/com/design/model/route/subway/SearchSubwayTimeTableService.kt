package com.design.model.route.subway

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface SearchSubwayTimeTableService {
    @GET("subwayTimeTable")
    fun getStationTimeTableData(
        @Query("apiKey") apikey: String,
        @Query("stationID") stationID: String
    ): Call<StationTimeTableDTO>
}