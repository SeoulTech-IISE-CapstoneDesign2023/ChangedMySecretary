package com.example.fastcampus.part3.design.util

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat
import com.example.fastcampus.part3.design.util.Key.Companion.NOTIFICATION_ID
import com.example.fastcampus.part3.design.alarm.NotificationReceiver
import com.example.fastcampus.part3.design.alarm.UpdateRouteService
import java.util.Calendar

object AlarmUtil {
    fun askNotificationPermission(
        activity: Activity,
        requestPermissionLauncher: ActivityResultLauncher<String>
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                //허용된거임 그러면 그냥 ㅇㅋ
            } else if (shouldShowRequestPermissionRationale(
                    activity,
                    Manifest.permission.POST_NOTIFICATIONS
                )
            ) {
                //여기는 거절하고 한번더 알려주는거
                showPermissionRationalDialog(activity, requestPermissionLauncher)
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun showPermissionRationalDialog(
        context: Context,
        requestPermissionLauncher: ActivityResultLauncher<String>
    ) {
        AlertDialog.Builder(context)
            .setMessage("알림 권한이 없으면 알림을 받을 수 없습니다.")
            .setPositiveButton("권한 허용하기") { _, _ ->
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
            .setNegativeButton("취소") { dialogInterface, _ ->
                dialogInterface.cancel()
            }
            .show()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun createAlarm(dateTime: String, context: Context, message: String) {
        val year = dateTime.substring(0, 4).toInt()
        val month = dateTime.substring(4, 6).toInt()
        val day = dateTime.substring(6, 8).toInt()
        val hour = dateTime.substring(8, 10).toInt()
        val minute = dateTime.substring(10, 12).toInt()
        Log.e("scheduleNotification", "$year $month $day $hour $minute")

        //알람시간을 위한 calendar 객체 생성
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month - 1)
        calendar.set(Calendar.DAY_OF_MONTH, day)
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)

        //현재 시간과 알람 시간을 비교해서 알람 시간이 과거면은 알람 설정
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            Log.e("fcmService", "현재시간보다 늦게 알람 설정은 안됨")
            return
        }

        val notificationId = dateTime.substring(3).toInt()
        Log.e("fcm", "notificationId $notificationId")
        scheduleNotification(calendar, context, notificationId, message)
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun scheduleNotification(
        calendar: Calendar,
        context: Context,
        notificationId: Int,
        message: String,
    ) {
        val alarmTime = calendar.timeInMillis
        Log.e("fcm", "${calendar.time}")
        calendar.add(Calendar.HOUR, -1) //1시간을 뺌
        Log.e("fcm", "${calendar.time}")
        val minusOneHour = calendar.timeInMillis
        //이동수단이 걷기가 아닐경우만 service를 통한 알람 업데이트 + 서비스를 불러오기로한 1시간전 시간이 현재시간보다 빠르게되면 그냥 지금 것을 사용
        if (minusOneHour > System.currentTimeMillis()) {
            val serviceIntent = Intent(context, UpdateRouteService::class.java)
            serviceIntent.putExtra(NOTIFICATION_ID, notificationId)
            val servicePendingIntent = PendingIntent.getService(
                context,
                notificationId,
                serviceIntent,
                PendingIntent.FLAG_IMMUTABLE
            )
            val serviceAlarmManger =
                context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            serviceAlarmManger.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                minusOneHour,
                servicePendingIntent
            )
        }else {
            //1시간전에 업데이트를 할 수 없다면은 그냥 바로 알람 설정해줌
            val notificationIntent = Intent(context, NotificationReceiver::class.java)
            notificationIntent.putExtra(Key.NOTIFICATION_ID, notificationId)
            notificationIntent.putExtra(Key.MESSAGE, message)

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId,
                notificationIntent,
                PendingIntent.FLAG_IMMUTABLE
            )

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            //알람을 주기위한 것
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    alarmTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    alarmTime,
                    pendingIntent
                )
            }
        }
    }

    fun deleteAlarm(notificationId: Int,context:Context) {
        FirebaseUtil.alarmDataBase.child(notificationId.toString()).removeValue()
        val notificationIntent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }
}