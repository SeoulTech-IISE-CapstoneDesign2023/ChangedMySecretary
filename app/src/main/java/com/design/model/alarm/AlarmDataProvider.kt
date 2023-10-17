package com.design.model.alarm

import com.design.util.FirebaseUtil

class AlarmDataProvider(private val callback: Callback) {

    fun getAlarmData(notificationId: String) {
        FirebaseUtil.alarmDataBase.child(notificationId).get()
            .addOnSuccessListener {
                val alarmData = it.getValue(AlarmItem::class.java)
                alarmData?.let { data ->
                    callback.loadAlarmData(data)
                }
            }
            .addOnFailureListener {  }
    }

    interface Callback {
        fun loadAlarmData(data: AlarmItem)
    }
}