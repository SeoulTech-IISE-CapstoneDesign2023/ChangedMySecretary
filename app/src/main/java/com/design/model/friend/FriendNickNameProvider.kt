package com.design.model.friend

import com.design.model.User
import com.design.util.FirebaseUtil
import com.design.util.Key.Companion.DB_USERS
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class FriendNickNameProvider(private val callback: Callback) {

    private val nickNameList = mutableListOf<Friend>()//친구들의 닉네임 저장

    fun getFriendNickName(query: String) {
        FirebaseUtil.userDataBase.child("friend_info").child("friends").get()
            .addOnSuccessListener {
                val data = it.value as Map<*, *>

                for ((key, value) in data) {
                    if (value == true) {
                        Firebase.database.reference.child(DB_USERS).child(key.toString())
                            .child("user_info").get()
                            .addOnSuccessListener {
                                val user = it.getValue(User::class.java)
                                val nickName = user?.nickname.toString()
                                nickNameList.add(Friend(nickName = nickName))
                                val filteredList = nickNameList.filter { item ->
                                    item.nickName.contains(query, ignoreCase = true)
                                }
                                callback.loadFriendNickNameList(
                                    filteredList.toSet().toMutableList()
                                )
                            }
                    }
                }

            }
    }

    interface Callback {
        fun loadFriendNickNameList(list: MutableList<Friend>)
    }
}