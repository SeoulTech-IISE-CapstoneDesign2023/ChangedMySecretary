package com.example.fastcampus.part3.design.model.route.bus.realLocation

data class Real(
    val busId: String,
    val busPlateNo: String,
    val busPosition: String,
    val endBusYn: String,
    val fromStationId: String,
    val fromStationSeq: String,
    val lowBusYn: String,
    val routeId: String,
    val seoulProvdTm: String,
    val toStationId: String,
    val toStationSeq: String
)