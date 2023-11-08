package com.design.model.route

import com.design.util.LocationRetrofitManager
import retrofit2.Call
import retrofit2.Response

class RouteProvider(private val callback: Callback) {

    fun getRoute(
        startX: Double,
        startY: Double,
        endX: Double,
        endY: Double
    ) {
        LocationRetrofitManager.searchRootService.getPublicTransitRoute(
            "YGJ//L1rg7VWJ2Gc79XXv4aS2Evs19Ai+Iu66hzJpts",
            startX.toString(),
            startY.toString(),
            endX.toString(),
            endY.toString()
        )
            .enqueue(object : retrofit2.Callback<PublicTransitRoute> {
                override fun onResponse(
                    call: Call<PublicTransitRoute>,
                    response: Response<PublicTransitRoute>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.let {
                            callback.loadRoute(it)
                        }
                    }
                }

                override fun onFailure(call: Call<PublicTransitRoute>, t: Throwable) {
                    t.printStackTrace()
                }

            })
    }

    interface Callback {
        fun loadRoute(data: PublicTransitRoute?)
    }
}