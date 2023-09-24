package com.example.fastcampus.part3.design.model.route

import com.example.fastcampus.part3.design.model.route.Path

data class Result(
    val busCount: Int,
    val endRadius: Int,
    val outTrafficCheck: Int,
    val path: List<Path>,
    val pointDistance: Int,
    val searchType: Int,
    val startRadius: Int,
    val subwayBusCount: Int,
    val subwayCount: Int
)