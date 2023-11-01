package com.design.view

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import java.util.Locale
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.TimeUnit

class TimerTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private companion object {
        const val DEFAULT_INTERVAL = 60000L // 1분
    }

    private var timer = Timer()
    private var endTime: Long = 0
    private var interval = DEFAULT_INTERVAL
    private var isCanceled = false
    private val handler = Handler(Looper.getMainLooper())

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopTimer()
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (visibility == VISIBLE) {
            startTimer()
        } else {
            stopTimer()
        }
    }

    fun setInterval(interval: Long) {
        if (interval >= 0) {
            this.interval = interval
            stopTimer()
            startTimer()
        }
    }

    fun setEndTime(endTime: Long) {
        if (endTime >= 0) {
            this.endTime = endTime
            stopTimer()
            startTimer()
        }
    }

    private fun startTimer() {
        if (endTime == 0L) {
            return
        }
        if (isCanceled) {
            timer = Timer()
            isCanceled = false
        }
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                handler.post {
                    setText(getDurationBreakdown(endTime - System.currentTimeMillis()))
                }
            }
        }, 0, interval)
    }

    private fun stopTimer() {
        timer.cancel()
        isCanceled = true
    }

    private fun getDurationBreakdown(diff: Long): String {
        var millis = diff
        if (millis < 0) {
            return "00:00:00"
        }
        val days = TimeUnit.MILLISECONDS.toDays(millis)
        millis -= TimeUnit.DAYS.toMillis(days)
        val hours = TimeUnit.MILLISECONDS.toHours(millis)
        millis -= TimeUnit.HOURS.toMillis(hours)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millis)
        return if(days == 0L){
            String.format(Locale.KOREA, "출발시간\n%02d시간%02d분\n남음", hours, minutes)
        }else if (hours == 0L){
            String.format(Locale.KOREA, "출발시간\n%02d분\n남음", minutes)
        }else if(minutes == 0L) {
            String.format(Locale.KOREA, "출발하셔야 합니다!")
        }else {
            String.format(Locale.KOREA, "출발시간\n%02d일%02d시간%02d분\n남음", days,hours, minutes)
        }

    }


}