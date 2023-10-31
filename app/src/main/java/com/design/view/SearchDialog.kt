package com.design.view

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import com.design.R
import com.design.User
import com.design.databinding.SearchDialogBinding
import com.design.util.FirebaseUtil
import com.design.util.Key.Companion.DB_USERS
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class SearchDialog(private val binding: SearchDialogBinding) : DialogFragment() {
    //todo reyclerView 연결해주기

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        val nickNameList = mutableListOf<String>()//친구들의 닉네임 저장
        dialogBuilder.setView(binding.root)
        //todo내 친구리스트에 친구 저장 후 어댑터에 연결 -> 프로바이더생성 후 친구닉네임을 저장해주자
        FirebaseUtil.userDataBase.child("friend_info").child("friends").get()
            .addOnSuccessListener {
                val data = it.value as Map<*, *>
                Log.e("dialog", data.toString())

                for ((key, value) in data) {
                    if (value == true) {
                        Firebase.database.reference.child(DB_USERS).child(key.toString())
                            .child("user_info").get()
                            .addOnSuccessListener {
                                val user = it.getValue(User::class.java)
                                nickNameList.add(user?.nickname.toString())
                                Log.e("dialog", user?.nickname.toString())
                            }
                    }
                }
            }
        //친구리스트에 있는 친구들의 닉네임 저장

        binding.textInputEditText.addTextChangedListener {

        }
        return dialogBuilder.create()
    }
}