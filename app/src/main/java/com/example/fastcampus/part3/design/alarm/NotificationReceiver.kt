package com.example.fastcampus.part3.design.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.fastcampus.part3.design.util.Key.Companion.MESSAGE
import com.example.fastcampus.part3.design.util.Key.Companion.NOTIFICATION_ID
import com.example.fastcampus.part3.design.MainActivity
import com.example.fastcampus.part3.design.R
import com.example.fastcampus.part3.design.util.FirebaseUtil
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class NotificationReceiver : BroadcastReceiver() {

    companion object {
        var mediaPlayer: MediaPlayer? = null
    }

    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationId = intent.getIntExtra(NOTIFICATION_ID, 0)
        val message = intent.getStringExtra(MESSAGE)

        //알람이 울리면은 파이어베이스에 있는 정보 삭제
        val userId = Firebase.auth.currentUser?.uid ?: ""

        //채널 생성
        val name = "출발 알림"
        val descriptionText = "출발 알림입니다."
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channelId = context.getString(R.string.default_notification_channel_id)
        val mChannel = NotificationChannel(channelId, name, importance)
        mChannel.description = descriptionText

        notificationManager.createNotificationChannel(mChannel)

        val notificationIntent = Intent(context, MainActivity::class.java)
        //이부분에 추가로 정보를 담을 수 있음
        //예를 들어 notificationIntent.putExtra("key","value") 이런식으로

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(
            context,
            context.getString(R.string.default_notification_channel_id)
        )
            .setSmallIcon(R.drawable.baseline_access_alarm_24)
            .setContentTitle("MapMyDay")
            .setContentText(message)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        //알림음 설정
//        mediaPlayer = MediaPlayer.create(context, R.raw.alarm)
//        mediaPlayer?.start()

        notificationManager.notify(notificationId, notificationBuilder.build())

        //알람이 나타나고 난후 firebase에서 데이터 삭제
        FirebaseUtil.alarmDataBase.child(notificationId.toString()).removeValue()
            .addOnSuccessListener {
                Log.e("notificationReceiver","알람이 실행후 데이터삭제")
            }
            .addOnFailureListener {
                it.printStackTrace()
                Log.e("notificationReceiver","알람이 실행후 데이터삭제 실패")
            }

    }
}