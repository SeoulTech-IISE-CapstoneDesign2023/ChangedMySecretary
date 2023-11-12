package com.design.model.friend

import android.util.Log
import com.design.model.Todo
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

    fun getSharedFriend(todoId : String?, year : String?, month : String?, day : String?){
// 우선은 나의 일상에서 friend info로 감 거기서 얻자
        FirebaseUtil.todoDataBase.child(year!!).child(month!!).child(day!!).child(todoId!!).get()
            .addOnSuccessListener { data ->
                val todo = data.getValue(Todo::class.java)
                val friendList = todo?.friendUid //친구 이름이 리스트형태로 들어옴
                val friendSet = mutableSetOf<Friend>()
                //친구의 uid를 통해서 nicknamelist를 만들어주자
                friendList?.forEach { friend ->
                    Firebase.database.reference.child(DB_USERS).child(friend).child("user_info").get()
                        .addOnSuccessListener {
                            val user = it.getValue(User::class.java)
                            val nickName = user?.nickname.toString()
                            friendSet.add(Friend(nickName = nickName))
                            callback.loadFriendNickNameList(friendSet.toMutableList())
                        }
                }

            }
    }

    interface Callback {
        fun loadFriendNickNameList(list: MutableList<Friend>)
    }
}