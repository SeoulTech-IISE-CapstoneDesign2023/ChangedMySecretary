package com.design.model.route.bus.realLocation

import com.design.util.LocationRetrofitManager
import retrofit2.Call
import retrofit2.Response

class BusRealTimeLocationProvider() {

    fun getBusRealTimeLocation(busId: Int, callback:(Int?)-> Unit) {
        LocationRetrofitManager.searchBusRealLocationService.getRouteId(
            "YGJ//L1rg7VWJ2Gc79XXv4aS2Evs19Ai+Iu66hzJpts",
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