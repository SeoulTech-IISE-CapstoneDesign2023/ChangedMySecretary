package com.design.model.route.bus.realtime

import com.design.util.LocationRetrofitManager
import retrofit2.Call
import retrofit2.Response

class BusRealTimeProvider() {

    fun getBusRealTime(stationId: Int, routeId: Int?,callback : (Int?) -> Unit) {
        if(routeId == null){
            callback(null)
        }else {
            LocationRetrofitManager.searchBusRealTimeService.getBusRealTime(
                "YGJ//L1rg7VWJ2Gc79XXv4aS2Evs19Ai+Iu66hzJpts",
                stationId
            )
                .enqueue(object : retrofit2.Callback<RealTimeArrivalBus> {
                    override fun onResponse(
                        call: Call<RealTimeArrivalBus>,
                        response: Response<RealTimeArrivalBus>
                    ) {
                        if (response.isSuccessful) {
                            response.body()?.let { data ->
                                val latestTime =
                                    data?.result?.real?.filter { it.localRouteId.toInt() == routeId }
                                        ?.map { it.arrival1.arrivalSec }?.firstOrNull() //초임
                                val latestTimeMinute = latestTime?.div(60)
                                callback(latestTimeMinute)

                            }
                        }
                    }

                    override fun onFailure(call: Call<RealTimeArrivalBus>, t: Throwable) {
                        t.printStackTrace()
                        callback(null)
                    }

                })
        }

    }
}