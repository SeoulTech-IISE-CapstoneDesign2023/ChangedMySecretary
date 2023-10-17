package com.design.model.route

data class ResultInfo(
    val trafficType: Int,
    var startName: String?,
    var endName: String?,
    val sectionTime: Int?,
    val lane: Int?,
    val busno: String?,
    val subwayCode: String?,
    val wayCode: Int?,
    var waitTime: Int?,
    var busId: Int?,
)