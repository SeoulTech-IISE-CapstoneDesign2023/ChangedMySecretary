package com.design.util

import com.design.util.Key.Companion.DB_ALARMS
import com.design.util.Key.Companion.DB_IMPORTANCE
import com.design.util.Key.Companion.DB_USERS
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

object FirebaseUtil {

    val userUid = Firebase.auth.currentUser?.uid ?: ""

    //알람 DB 연결
    val alarmDataBase = Firebase.database.reference.child(DB_ALARMS).child(userUid)

    //캘린더 DB 연결
    val todoDataBase = Firebase.database.reference.child(Key.DB_CALENDAR).child(userUid)
    //즐겨찾기 DB 연결
    val importanceDataBase = Firebase.database.reference.child(DB_IMPORTANCE).child(userUid)
    //user DB 연결
    val userDataBase = Firebase.database.reference.child(DB_USERS).child(userUid)
    //태그 DB 연결
    val tagDataBase = Firebase.database.reference.child(Key.DB_TAG).child(userUid)


}