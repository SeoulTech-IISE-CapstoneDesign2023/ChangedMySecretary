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
import com.design.model.friend.FriendNickNameProvider
import com.design.util.FirebaseUtil
import com.design.util.Key.Companion.DB_USERS
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class SearchDialog(private val binding: SearchDialogBinding) : DialogFragment(), FriendNickNameProvider.Callback {
    //todo reyclerView 연결해주기
    private val friendNickNameProvider = FriendNickNameProvider(this)

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setView(binding.root)
        //todo내 친구리스트에 친구 저장 후 어댑터에 연결 -> 프로바이더생성 후 친구닉네임을 저장해주자
        friendNickNameProvider.getFriendNickName()
        //친구리스트에 있는 친구들의 닉네임 저장

        binding.textInputEditText.addTextChangedListener {

        }

        binding.cancelButton.setOnClickListener {
            dialog?.dismiss()
        }

        binding.okButton.setOnClickListener {
            dialog?.dismiss()
        }
        return dialogBuilder.create()
    }

    override fun loadFriendNickNameList(list: MutableList<String>) {
        //이부분에서 정보가 넘어오게되면 여기서 recyclerview를 업데이트 해줘야한다
    }
}