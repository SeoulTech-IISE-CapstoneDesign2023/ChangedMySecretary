package com.example.fastcampus.part3.design.model.route.bus.realtime

import com.example.fastcampus.part3.design.model.route.bus.realtime.Base
import com.example.fastcampus.part3.design.model.route.bus.realtime.Real

data class Result(
    val base: Base,
    val real: List<Real>
)