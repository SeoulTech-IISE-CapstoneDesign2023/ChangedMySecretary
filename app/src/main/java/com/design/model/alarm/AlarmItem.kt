package com.design.model.alarm

data class AlarmItem(
    var notificationId: String? = null, //알람이 울리는 시간에서 앞에서 3자리를뺀 나머지
    val startLat: Double? = null,
    val startLng: Double? = null,
    val arrivalLat: Double? = null,
    val arrivalLng: Double? = null,
    var appointmentTime: String? = null,
    val type: String? = null, //이동수단,
    val startPlace: String? = null,
    val arrivalPlace: String? = null,
    val dateTime: String? = null,
    val message: String? = null,
    val readyTime: String? = null,
    val todoId: String? = null,
)
