package com.example.fastcampus.part3.design.model.route

import com.example.fastcampus.part3.design.model.route.Lane
import com.example.fastcampus.part3.design.model.route.PassStopList

data class SubPath(
    val distance: Int,
    val door: String,
    val endArsID: String,
    val endExitNo: String,
    val endExitX: Double,
    val endExitY: Double,
    val endID: Int,
    val endLocalStationID: String,
    var endName: String,
    val endStationCityCode: Int,
    val endStationProviderCode: Int,
    val endX: Double,
    val endY: Double,
    val lane: List<Lane>,
    val passStopList: PassStopList,
    val sectionTime: Int,
    val startArsID: String,
    val startExitNo: String,
    val startExitX: Double,
    val startExitY: Double,
    val startID: Int,
    val startLocalStationID: String,
    var startName: String,
    val startStationCityCode: Int,
    val startStationProviderCode: Int,
    val startX: Double,
    val startY: Double,
    val stationCount: Int,
    val trafficType: Int,
    val way: String,
    val wayCode: Int
)