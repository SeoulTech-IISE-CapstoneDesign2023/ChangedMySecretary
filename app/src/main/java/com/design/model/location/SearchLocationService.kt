package com.design.model.location

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface SearchLocationService {

    @GET("tmap/pois")
    fun getLocation(
        @Query("version") version: String = "1",
        @Query("searchKeyword") searchKeyword: String,
        @Query("reqCoordType") reqCoordType: String = "WGS84GEO",
        @Query("resCoordType") resCoordType: String = "WGS84GEO",
        @Query("count") count: Int,
    ): Call<Location>
}