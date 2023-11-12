package com.design.view

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import com.design.adapter.FriendNickNameListAdapter
import com.design.databinding.ShareFriendDialogBinding
import com.design.model.friend.Friend
import com.design.model.friend.FriendNickNameProvider
import java.util.Objects

class SharedFriendDialog(
    private val binding: ShareFriendDialogBinding,
    private val todoId: String?,
    private val year: String?,
    private val month: String?,
    private val day: String?
) : DialogFragment(),
    FriendNickNameProvider.Callback {
    private val friendNickNameProvider = FriendNickNameProvider(this)

    private lateinit var adapter: FriendNickNameListAdapter

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setView(binding.root)
        binding.root.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setAdapter()

        friendNickNameProvider.getSharedFriend(todoId, year, month, day)

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