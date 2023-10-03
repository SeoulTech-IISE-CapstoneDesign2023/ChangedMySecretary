package com.example.fastcampus.part3.design.model.route

import com.example.fastcampus.part3.design.util.LocationRetrofitManager
import retrofit2.Call
import retrofit2.Response

class RouteProvider(private val callback:Callback) {

    fun getRoute(startX: Double,
                 startY: Double,
                 endX: Double,
                 endY: Double){
        LocationRetrofitManager.searchRootService.getPublicTransitRoute(
            "HFzt2MlKKNAzow6eacQK7TsnOIrG0jNcK5vZ3FV9mEQ",
            startX.toString(),
            startY.toString(),
            endX.toString(),
            endY.toString())
            .enqueue(object : retrofit2.Callback<PublicTransitRoute>{
                override fun onResponse(
                    call: Call<PublicTransitRoute>,
                    response: Response<PublicTransitRoute>
                ) {
                    if(response.isSuccessful){
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