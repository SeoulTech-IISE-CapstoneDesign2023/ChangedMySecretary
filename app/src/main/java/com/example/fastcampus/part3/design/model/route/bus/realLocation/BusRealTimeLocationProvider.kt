package com.example.fastcampus.part3.design.model.route.bus.realLocation

import com.example.fastcampus.part3.design.LocationRetrofitManager
import retrofit2.Call
import retrofit2.Response

class BusRealTimeLocationProvider() {

    fun getBusRealTimeLocation(busId: Int, callback:(Int?)-> Unit) {
        LocationRetrofitManager.searchBusRealLocationService.getRouteId(
            "HFzt2MlKKNAzow6eacQK7TsnOIrG0jNcK5vZ3FV9mEQ",
            busId
        )
            .enqueue(object : retrofit2.Callback<BusRealTimeLocationDTO> {
                override fun onResponse(
                    call: Call<BusRealTimeLocationDTO>,
                    response: Response<BusRealTimeLocationDTO>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.let {data ->
                            val routeId = data?.result?.real?.filter { it.busId == busId.toString() }?.map { it.routeId }?.firstOrNull()
                            callback(routeId?.toInt())
                        }
                    }
                }

                override fun onFailure(call: Call<BusRealTimeLocationDTO>, t: Throwable) {
                    t.printStackTrace()
                    callback(null)
                }

            })
    }
}