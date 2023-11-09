package com.design.view

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import com.design.adapter.FriendNickNameListAdapter
import com.design.databinding.ShareFriendDialogBinding
import com.design.model.friend.Friend
import com.design.model.friend.FriendNickNameProvider

class SharedFriendDialog(private val binding: ShareFriendDialogBinding) : DialogFragment(),
    FriendNickNameProvider.Callback {
    private val friendNickNameProvider = FriendNickNameProvider(this)

    private lateinit var adapter: FriendNickNameListAdapter

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setView(binding.root)
        setAdapter()

        friendNickNameProvider.getFriendNickName("")

        return dialogBuilder.create()
    }

    private fun setAdapter() {
        adapter = FriendNickNameListAdapter { }
        binding.recyclerView.adapter = adapter
    }

    override fun loadFriendNickNameList(list: MutableList<Friend>) {
        //이부분에서 정보가 넘어오게되면 여기서 recyclerview를 업데이트 해줘야한다
        adapter.submitList(list)
        adapter.notifyDataSetChanged()
        binding.emptyTextView.isVisible = list.size == 0
    }
}