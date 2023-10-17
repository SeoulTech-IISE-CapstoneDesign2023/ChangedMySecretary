package com.design.model.route.bus.realtime

import com.design.model.route.bus.realtime.Base
import com.design.model.route.bus.realtime.Real

data class Result(
    val base: Base,
    val real: List<Real>
)