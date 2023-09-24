package com.example.fastcampus.part3.design.model.car

data class Properties(
    val arrivalTime: String, //도착시간
    val departureTime: String, //출발해야할 시간
    val taxiFare: Int,
    val totalFare: Int,
    val totalTime: Int
)
