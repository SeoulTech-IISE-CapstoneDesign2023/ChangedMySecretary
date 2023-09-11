package com.example.fastcampus.part3.design.model

data class Todo (
    val id : Int,
    val date : String,
    val todo : String,
    val type : Type,
    val importance : Boolean,
)

enum class Type { COMPLETE, READY }