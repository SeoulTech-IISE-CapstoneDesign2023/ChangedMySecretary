package com.design.model.friend

import com.design.User
import com.design.util.FirebaseUtil
import com.design.util.Key.Companion.DB_USERS
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class FriendNickNameProvider(private val callback: Callback) {

    private val nickNameList = mutableListOf<String>()//친구들의 닉네임 저장

    fun getFriendNickName() {
        FirebaseUtil.userDataBase.child("friend_info").child("friends").get()
            .addOnSuccessListener {
                val data = it.value as Map<*, *>

                for ((key, value) in data) {
                    if (value == true) {
                        Firebase.database.reference.child(DB_USERS).child(key.toString())
                            .child("user_info").get()
                            .addOnSuccessListener {
                                val user = it.getValue(User::class.java)
                                nickNameList.add(user?.nickname.toString())
                            }
                    }
                }
                callback.loadFriendNickNameList(nickNameList)
            }
    }

    interface Callback {
        fun loadFriendNickNameList(list: MutableList<String>)
    }
}