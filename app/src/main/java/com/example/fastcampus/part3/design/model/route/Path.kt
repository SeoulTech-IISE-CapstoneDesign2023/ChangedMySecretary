package com.example.fastcampus.part3.design.model.route

data class Path(
    val info: Info,
    val pathType: Int,
    val subPath: List<SubPath>
)