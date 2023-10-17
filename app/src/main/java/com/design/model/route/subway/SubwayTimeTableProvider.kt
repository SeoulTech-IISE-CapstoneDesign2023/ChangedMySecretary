package com.design.model.route.subway

import com.design.util.LocationRetrofitManager
import retrofit2.Call
import retrofit2.Response
import java.util.Calendar

class SubwayTimeTableProvider {

    private var latestTime: Int? = null

    fun getSubwayTimeTable(
        stationId: String,
        wayCode: Int,
        callback: (Int?) -> Unit
    ) {
        LocationRetrofitManager.searchSubwayTimeTableService.getStationTimeTableData(
            "YGJ//L1rg7VWJ2Gc79XXv4aS2Evs19Ai+Iu66hzJpts",
            stationId
        )
            .enqueue(object : retrofit2.Callback<StationTimeTableDTO> {
                override fun onResponse(
                    call: Call<StationTimeTableDTO>,
                    response: Response<StationTimeTableDTO>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.let { data ->
                            //여기서 waycode를 활용하여 최신 시간을 return subpath에서 받아오게됨

                            val waitingTimes: MutableList<String> = mutableListOf()
                            val currentTime = Calendar.getInstance()
                            var hour = currentTime.get(Calendar.HOUR_OF_DAY)//핸드폰 현재 시
                            var minute = currentTime.get(Calendar.MINUTE)// 현드폰 현재 분 + 앞에있는 경로 시간
                            when (wayCode) {
                                1 -> {
                                    data?.result?.OrdList?.up?.time?.forEach { time ->
                                        findLatestTime(time, hour, waitingTimes, minute)
                                    }
                                    setLatestTime(waitingTimes, minute, callback)
                                }

                                2 -> {
                                    data?.result?.OrdList?.down?.time?.forEach { time ->
                                        findLatestTime(time, hour, waitingTimes, minute)
                                    }
                                    setLatestTime(waitingTimes, minute, callback)
                                }

                                else -> {
                                    callback(null)
                                }
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<StationTimeTableDTO>, t: Throwable) {
                    t.printStackTrace()
                    callback(null)
                }

            })
    }

    private fun findLatestTime(
        time: Time,
        hour: Int,
        waitingTimes: MutableList<String>,
        minute: Int
    ) {
        if (time.Idx == hour) {
            val timeTable = time.list // 해당 시간에 맞는 지하철 시간표
            val regex = Regex("\\d+\\([^)]+\\)")
            waitingTimes.addAll(
                regex.findAll(timeTable)
                    .mapNotNull { matchResult ->
                        val timeString =
                            matchResult.value // ex) "04(동두천)"
                        val time = timeString.substringBefore('(')
                            .toInt() // 분 부분 추출
                        if (minute < time) {
                            timeString.substring(0, 2)
                        } else {
                            null
                        }
                    }
            )
        } else if ((time.Idx) - 1 == hour) {
            val timeTable = time.list // 해당 시간에 맞는 지하철 시간표
            val regex = Regex("\\d+\\([^)]+\\)")
            waitingTimes.addAll(
                regex.findAll(timeTable)
                    .mapNotNull { matchResult ->
                        val timeString =
                            matchResult.value // ex) "04(동두천)"
                        timeString.substring(0, 2)
                    }
            )
        }
    }

    private fun setLatestTime(
        waitingTimes: MutableList<String>,
        minute: Int,
        callback: (Int?) -> Unit
    ) {
        if (waitingTimes.isNotEmpty()) {
            latestTime = if ((waitingTimes[0].toInt() - minute) >= 0) {
                waitingTimes[0].toInt() - minute
            } else {
                (waitingTimes[0].toInt() + 60) - minute
            }
            callback(latestTime)
        } else {
            callback(null)
        }
    }
}