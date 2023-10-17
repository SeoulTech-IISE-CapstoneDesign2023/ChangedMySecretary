package com.design.model.route

data class Path(
    val info: Info,
    val pathType: Int,
    val subPath: List<SubPath>
)