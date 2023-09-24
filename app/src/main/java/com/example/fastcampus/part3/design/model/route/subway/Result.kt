package com.example.fastcampus.part3.design.model.route.subway

data class Result(
    val OrdList: OrdList,
    val SatList: SatList,
    val SunList: SunList,
    val downWay: String,
    val laneCity: String,
    val laneName: String,
    val stationID: Int,
    val stationName: String,
    val type: Int,
    val upWay: String
)