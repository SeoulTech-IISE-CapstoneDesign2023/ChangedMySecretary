package com.example.fastcampus.part3.design.model.route.subway

import android.util.Log
import com.example.fastcampus.part3.design.CreateActivity
import com.example.fastcampus.part3.design.LocationRetrofitManager
import retrofit2.Call
import retrofit2.Response
import java.util.Calendar

class SubwayTimeTableProvider() {

    private var latestTime: Int? = null

    fun getSubwayTimeTable(stationId: String, wayCode: Int, plusTime: Int = 0,callback: (Int?) -> Unit) {
        LocationRetrofitManager.searchSubwayTimeTableService.getStationTimeTableData(
            "HFzt2MlKKNAzow6eacQK7TsnOIrG0jNcK5vZ3FV9mEQ",
            stationId
        )
            .enqueue(object : retrofit2.Callback<StationTimeTableDTO> {
                override fun onResponse(
                    call: Call<StationTimeTableDTO>,
                    response: Response<StationTimeTableDTO>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.let {data ->
                            //여기서 waycode를 활용하여 최신 시간을 return subpath에서 받아오게됨

                            val waitingTimes: MutableList<String> = mutableListOf()
                            val currentTime = Calendar.getInstance()
                            var hour = currentTime.get(Calendar.HOUR_OF_DAY)//핸드폰 현재 시
                            var minute =
                                currentTime.get(Calendar.MINUTE) + plusTime// 현드폰 현재 분 + 앞에있는 경로 시간

                            if (minute >= 60) {
                                hour += 1
                                minute -= 60
                            }
                            when (wayCode) {
                                1 -> {
                                    data?.result?.OrdList?.up?.time?.forEach { time ->
                                        if (time.Idx == hour || (time.Idx) - 1 == hour) {
                                            val timeTable = time.list // 해당 시간에 맞는 지하철 시간표
                                            val regex = Regex("\\d+\\([^)]+\\)")
                                            waitingTimes.addAll(
                                                regex.findAll(timeTable)
                                                    .mapNotNull { matchResult ->
                                                        val timeString = matchResult.value // ex) "04(동두천)"
                                                        val time = timeString.substringBefore('(').toInt() // 분 부분 추출
                                                        if (minute < time) {
                                                            timeString.substring(0, 2)
                                                        } else {
                                                            null
                                                        }
                                                    }
                                            )
                                        }
                                    }
                                    if (waitingTimes.isNotEmpty()) {
                                        latestTime = if ((waitingTimes[0].toInt() - minute) >= 0) {
                                            waitingTimes[0].toInt() - minute
                                        } else {
                                            minute + 60 - waitingTimes[0].toInt()
                                        }
                                        callback(latestTime)
                                    } else {
                                        callback(latestTime)
                                    }
                                }
                                2 -> {
                                    data?.result?.OrdList?.down?.time?.forEach { time ->
                                        if (time.Idx == hour || (time.Idx) - 1 == hour) {
                                            val timeTable = time.list // 해당 시간에 맞는 지하철 시간표
                                            val regex = Regex("\\d+\\([^)]+\\)")
                                            waitingTimes.addAll(
                                                regex.findAll(timeTable)
                                                    .mapNotNull { matchResult ->
                                                        val timeString = matchResult.value // ex) "04(동두천)"
                                                        val time =
                                                            timeString.substringBefore('(').toInt() // 분 부분 추출
                                                        if (minute < time) {
                                                            timeString.substring(0, 2)
                                                        } else {
                                                            null
                                                        }
                                                    }
                                            )
                                        }
                                    }
                                    if (waitingTimes.isNotEmpty()) {
                                        latestTime = if ((waitingTimes[0].toInt() - minute) >= 0) {
                                            waitingTimes[0].toInt() - minute
                                        } else {
                                            minute + 60 - waitingTimes[0].toInt()
                                        }
                                        callback(latestTime)
                                    } else {
                                        callback(null)
                                    }
                                }
                                else -> {callback(null)}
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
}