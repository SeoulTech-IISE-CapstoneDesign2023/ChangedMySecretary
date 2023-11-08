package com.design.model.tag

import java.util.ArrayList

data class Tag(
    var tagId : String? = null,
    var todoId : String? = null,
    val title : String? = null,
    val date : String? = null,
    val place : String? = null,
    val endY : Double? = null,
    val endX : Double? = null,
    val usingShare: Boolean? =null,
    val friendUid: ArrayList<String>? =null,
)