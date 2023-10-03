package com.example.fastcampus.part3.design.util

import com.example.fastcampus.part3.design.util.Key.Companion.DB_ALARMS
import com.example.fastcampus.part3.design.util.Key.Companion.DB_USERS
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

object FirebaseUtil {

    val userUid = Firebase.auth.currentUser?.uid ?: ""

    //알람 db연결
    val alarmDataBase = Firebase.database.reference.child(DB_ALARMS).child(userUid)

    val userDataBase = Firebase.database.reference.child(DB_USERS).child(userUid)


}