package com.example.fastcampus.part3.design.model.car

import com.example.fastcampus.part3.design.LocationRetrofitManager
import retrofit2.Call
import retrofit2.Response

class CarRouteProvider(private val callback : Callback) {

    fun getCarRoot(body : CarRouteRequest){
        LocationRetrofitManager.searchCarService.getCarRoute(request = body)
            .enqueue(object : retrofit2.Callback<Dto>{
                override fun onResponse(call: Call<Dto>, response: Response<Dto>) {
                    if(response.isSuccessful){
                        response.body()?.let {
                            callback.loadCarRoot(it)
                        }
                    }
                }

                override fun onFailure(call: Call<Dto>, t: Throwable) {
                    t.printStackTrace()
                }

            })

    }

    interface Callback {
        fun loadCarRoot(data : Dto)
    }
}