package com.design.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.design.util.AlarmUtil
import com.design.util.FirebaseUtil

class RestartAlarmReceiver : BroadcastReceiver() {

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onReceive(context: Context, intent: Intent) {
        //어플이 재시동 되거나 핸드폰을 껐다 켯을때
        if (intent.action.equals("android.intent.action.BOOT_COMPLETED")) {
            FirebaseUtil.alarmDataBase.get()
                .addOnSuccessListener {
                    val dataSnapshot = it.value as Map<*, *>
                    val alarmList = dataSnapshot.values.toList()
                    alarmList.forEach { alarm ->
                        val alarmItem = alarm as Map<*, *>
                        val time = alarmItem["appointmentTime"] as String
                        val message = alarmItem["message"] as String
                        AlarmUtil.createAlarm(time, context, message)
                    }
                }
        }
    }
}