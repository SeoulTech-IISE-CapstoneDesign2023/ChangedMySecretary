package com.design.model.walk

import com.design.util.LocationRetrofitManager
import retrofit2.Call
import retrofit2.Response

class WalkingRouteProvider(private val callback: Callback) {
    fun getWalkingRoot(body: RouteData) {
        LocationRetrofitManager.searchWalkService.getWalkingTime(request = body)
            .enqueue(object : retrofit2.Callback<Dto> {
                override fun onResponse(call: Call<Dto>, response: Response<Dto>) {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            callback.loadWalkingRoot(it)
                        }
                    }
                }

                override fun onFailure(call: Call<Dto>, t: Throwable) {
                    t.printStackTrace()
                }

            })
    }

    interface Callback {
        fun loadWalkingRoot(data: Dto)
    }
}