package com.design.model.route

data class Lane(
    val busID: Int,
    val busNo: String,
    val name: String,
    val subwayCityCode: Int,
    val subwayCode: Int,
    val type: Int
)