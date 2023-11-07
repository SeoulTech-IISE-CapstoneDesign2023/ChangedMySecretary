package com.design.util

import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object TimeUtil {
    fun formatTotalTime(totalTime: Int): String {
        val hours = (totalTime / 60) / 60
        val minutes = (totalTime / 60) % 60
        return if (hours != 0) "약 $hours 시간 $minutes 분" else "약 $minutes 분"
    }

    fun parseDateTime(dateTime: String): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ")
        val zonedDateTime = ZonedDateTime.parse(dateTime, formatter)
            .withZoneSameInstant(ZoneId.of("Asia/Seoul"))
        return zonedDateTime.format(DateTimeFormatter.ofPattern("MM/dd HH : mm"))
    }

    fun convertToISODateTime(date: String): String {
        val inputFormat =
            SimpleDateFormat("yyyy년 MM월 dd일 (EEE), HH:mm", Locale.getDefault()) // 입력 형식 지정
        val outputFormat =
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()) // 출력 형식 지정

        try {
            val date = inputFormat.parse(date) // 문자열을 Date 객체로 변환
            return outputFormat.format(date) // ISO 8601 형식으로 변환한 문자열 반환
        } catch (e: Exception) {
            return "날짜 형식이 올바르지 않습니다."
        }
    }

    //준비시간을 저장하기 위한 util
    fun openTimePickerForReadyTime(readyTime: String): MaterialTimePicker {
        val preReadyTime = if (readyTime == "") "00:10" else readyTime
        val preHour = preReadyTime.substring(0, 2).toInt()
        val preMinute = preReadyTime.substring(3, 5).toInt()

        return MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(preHour)
            .setMinute(preMinute)
            .setTitleText("준비시간을 설정해주세요.")
            .setInputMode(MaterialTimePicker.INPUT_MODE_KEYBOARD)
            .build()
    }
}