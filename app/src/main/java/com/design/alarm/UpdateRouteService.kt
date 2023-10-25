package com.design.alarm

import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import com.design.util.Key.Companion.NOTIFICATION_ID
import com.design.model.Type
import com.design.model.alarm.AlarmDataProvider
import com.design.model.alarm.AlarmItem
import com.design.model.car.CarRouteProvider
import com.design.model.car.CarRouteRequest
import com.design.model.car.DepartureInfo
import com.design.model.car.DestinationInfo
import com.design.model.car.RoutesInfo
import com.design.model.route.PublicTransitRoute
import com.design.model.route.RouteProvider
import com.design.model.walk.Dto
import com.design.model.walk.RouteData
import com.design.model.walk.WalkingRouteProvider
import com.design.util.AlarmUtil
import com.design.util.FirebaseUtil
import com.design.util.TimeUtil
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class UpdateRouteService : Service(), AlarmDataProvider.Callback, WalkingRouteProvider.Callback,
    CarRouteProvider.Callback, RouteProvider.Callback {

    private val alarmDataProvider = AlarmDataProvider(this)
    private val walkingRouteProvider = WalkingRouteProvider(this)
    private val carRouteProvider = CarRouteProvider(this)
    private val routeProvider = RouteProvider(this)
    private val dateFormat = SimpleDateFormat("yyyy년 MM월 dd일 (EEE), HH:mm", Locale.KOREA)

    private var appointmentTime: String? = null
    private var newAppointmentTime: String? = null
    private var notificationId = ""
    private var newNotificationId = ""
    private var alarmData = mutableMapOf<String, Any>()
    private var startPlace = ""
    private var arrivalPlace = ""
    private var startX = 0.0
    private var startY = 0.0
    private var endX = 0.0
    private var endY = 0.0
    private var type: String? = null
    private var dateTime = ""
    private var message = ""
    private var readyTime = ""
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        //todo 길찾기 업데이트
        notificationId = intent.getIntExtra(NOTIFICATION_ID, 0).toString()
        //기존의 notificationId를 통해 정보를 가져온다.
        alarmDataProvider.getAlarmData(notificationId)
        return START_NOT_STICKY
    }

    override fun loadAlarmData(data: AlarmItem) {
        message = data.message ?: ""
        startPlace = data.startPlace ?: ""
        arrivalPlace = data.arrivalPlace ?: ""
        type = data.type
        startX = data.startLng ?: 0.0
        startY = data.startLat ?: 0.0
        endX = data.arrivalLng ?: 0.0
        endY = data.arrivalLat ?: 0.0
        appointmentTime = data.appointmentTime ?: ""
        dateTime = data.dateTime ?: ""
        readyTime = data.readyTime ?: ""
        when (type) {
            Type.CAR -> {
                val isoDateTime = TimeUtil.convertToISODateTime(dateTime!!)
                val body = CarRouteRequest(
                    routesInfo = RoutesInfo(
                        departure = DepartureInfo(
                            name = "출발지",
                            lon = startX.toString(),
                            lat = startY.toString()
                        ),
                        destination = DestinationInfo(
                            name = "도착지",
                            lon = endX.toString(),
                            lat = endY.toString()
                        ),
                        predictionType = "departure",
                        predictionTime = "$isoDateTime+0900"
                    )
                )
                carRouteProvider.getCarRoot(body)
            }

            Type.WALK -> {
                val body = RouteData(
                    startX = startX,
                    startY = startY,
                    endX = endX,
                    endY = endY,
                    startName = "%EC%B6%9C%EB%B0%9C%EC%A7%80",
                    endName = "%EB%8F%84%EC%B0%A9%EC%A7%80",
                    searchOption = 4
                )
                walkingRouteProvider.getWalkingRoot(body)
            }

            Type.PUBLIC -> {
                routeProvider.getRoute(startX, startY, endX, endY)
            }

            else -> {}
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun loadWalkingRoot(data: Dto) {
        AlarmUtil.createAlarm(appointmentTime!!, this, message)
        stopSelf()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun loadCarRoot(data: com.design.model.car.Dto) {
        val result = data.features?.map { it.properties }?.firstOrNull() ?: return
        val date = dateFormat.parse(dateTime)
        val readyHour = readyTime.substring(0, 2).toInt()
        val readyMinute = readyTime.substring(3, 5).toInt()
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.HOUR_OF_DAY, -readyHour)
        calendar.add(Calendar.MINUTE, -readyMinute)
        calendar.add(Calendar.SECOND, -result.totalTime)
        setAlarm(calendar)
        createAlarm()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun loadRoute(data: PublicTransitRoute?) {
        val minTimePath = data?.result?.path?.minByOrNull { it.info.totalTime }
        val date = dateFormat.parse(dateTime)
        val calendar = Calendar.getInstance()
        val readyHour = readyTime.substring(0, 2).toInt()
        val readyMinute = readyTime.substring(3, 5).toInt()
        calendar.add(Calendar.HOUR_OF_DAY, -readyHour)
        calendar.add(Calendar.MINUTE, -readyMinute)
        calendar.time = date
        calendar.add(Calendar.SECOND, -(minTimePath?.info?.totalTime!! * 60))
        setAlarm(calendar)
        createAlarm()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun createAlarm() {
        //알람을 설정하고
        newAppointmentTime?.let { AlarmUtil.createAlarm(it, this, message) }
        //기존 데이터를 삭제하고
        FirebaseUtil.alarmDataBase.child(notificationId).removeValue()
            .addOnSuccessListener {
                Log.e("updateService", "루트 업데이트로 인한 기존 알람정보 삭제")
            }
        //data를 업데이트
        FirebaseUtil.alarmDataBase.child(newNotificationId).updateChildren(alarmData)
        stopSelf()
    }

    private fun setAlarm(calendar: Calendar) {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1 // 월은 0부터 시작하므로 1을 더함
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        newAppointmentTime =
            String.format("%04d%02d%02d%02d%02d", year, month, day, hour, minute) //202309160940
        newNotificationId = newAppointmentTime!!.substring(3)
        alarmData = mutableMapOf()
        alarmData["startLng"] = startX
        alarmData["startLat"] = startY
        alarmData["arrivalLng"] = endX
        alarmData["arrivalLat"] = endY
        alarmData["startPlace"] = startPlace
        alarmData["arrivalPlace"] = arrivalPlace
        alarmData["type"] = type.toString()
        alarmData["notificationId"] = newNotificationId
        alarmData["appointmentTime"] = newAppointmentTime!!
        alarmData["dateTime"] = dateTime
        alarmData["message"] = message
        alarmData["readyTime"] = readyTime
    }
}