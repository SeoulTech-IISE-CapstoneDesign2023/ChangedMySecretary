package com.example.fastcampus.part3.design.model.location

import com.example.fastcampus.part3.design.util.LocationRetrofitManager
import retrofit2.Call
import retrofit2.Response

class LocationProvider(private val callback : Callback) {

    fun searchLocation(searchKeyWord : String) {
        LocationRetrofitManager.searchLocationService.getLocation("1",
            searchKeyWord,
            "WGS84GEO",
            "WGS84GEO",
            200)
            .enqueue(object : retrofit2.Callback<Location> {
                override fun onResponse(call: Call<Location>, response: Response<Location>) {
                    if(response.isSuccessful){
                        response.body()?.let {
                            callback.loadLocation(it)
                        }
                    }
                }

                override fun onFailure(call: Call<Location>, t: Throwable) {
                    t.printStackTrace()
                }

            })
    }

    interface Callback {
        fun loadLocation(data : Location)
    }
}