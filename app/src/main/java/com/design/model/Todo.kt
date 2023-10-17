package com.design.model

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.RequiresApi

data class Todo(
    var todoId: String = "",            // 일정 구별 아이디
    val title: String = "",             // 일정 제목
    val stDate: String = "",           // 일정 시작 날짜
    val stTime: String = "",           // 일정 시작 시간
    val enDate: String? = null,         // 일정 종료 날짜
    val enTime: String? = null,         // 일정 종료 시간
    val place: String? = null,          // 일정 장소
    val memo: String? = null,           // 일정 관련 메모
    var importance : Boolean? = false,           // 중요도
    val startPlace: String? = null,     // 일정 출발지
    val arrivePlace: String? = null,    // 일정 도착지
    val trackTime: String? = null,      // 알림 추적 시간
    val notificationId: String? = null, // 알람 구별 아이디
    val startLat: Double? = null,       // 출발 위도?
    val startLng: Double? = null,       // 출발 경도?
    val arrivalLat: Double? = null,     // 도착 위도
    val arrivalLng: Double? = null,     // 도착 경도
    val usingAlarm: Boolean? = null,  // 알람 사용 여부
)
    : Parcelable {
    @RequiresApi(Build.VERSION_CODES.Q)
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        importance = parcel.readBoolean(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        startLat = parcel.readDouble(),
        startLng = parcel.readDouble(),
        arrivalLat = parcel.readDouble(),
        arrivalLng = parcel.readDouble(),
        usingAlarm = parcel.readBoolean()
    )

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(todoId)
        parcel.writeString(title)
        parcel.writeString(stDate)
        parcel.writeString(stTime)
        parcel.writeString(enDate)
        parcel.writeString(enTime)
        parcel.writeString(place)
        parcel.writeString(memo)
        importance?.let { parcel.writeBoolean(it) }
        parcel.writeString(startPlace)
        parcel.writeString(arrivePlace)
        parcel.writeString(trackTime)
        parcel.writeString(notificationId)
        startLat?.let { parcel.writeDouble(it) }
        startLng?.let { parcel.writeDouble(it) }
        arrivalLat?.let { parcel.writeDouble(it) }
        arrivalLng?.let { parcel.writeDouble(it) }
        usingAlarm?.let { parcel.writeBoolean(it) }
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Todo> {
        @RequiresApi(Build.VERSION_CODES.Q)
        override fun createFromParcel(parcel: Parcel): Todo {
            return Todo(parcel)
        }

        override fun newArray(size: Int): Array<Todo?> {
            return arrayOfNulls(size)
        }
    }
}