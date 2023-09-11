package com.example.fastcampus.part3.design.model

data class ImageResponse(
    val id: String,
    val urls: UrlResponse,
    val color: String,

    )

data class UrlResponse(
    val raw: String,
    val full: String,
    val regular: String,
    val small: String,
    val thumb: String,
)

data class Image(
    val url: String,
    val color: String,
)
