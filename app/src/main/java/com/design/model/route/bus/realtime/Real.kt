package com.design.model.route.bus.realtime

data class Real(
    val arrival1: Arrival1,
    val arrival2: Arrival1,
    val localRouteId: String,
    val routeId: String,
    val routeNm: String,
    val stationSeq: String,
    val updownFlag: String
)