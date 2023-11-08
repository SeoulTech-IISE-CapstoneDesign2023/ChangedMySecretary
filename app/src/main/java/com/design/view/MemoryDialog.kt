package com.design.view

import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import com.design.MapFragment
import com.design.adapter.FriendNickNameListAdapter
import com.design.databinding.MemoryDialogBinding
import com.design.model.friend.Friend
import com.design.model.friend.FriendNickNameProvider
import com.google.android.material.chip.Chip

class MemoryDialog(private val binding: MemoryDialogBinding, private val dataTransferListener: MapFragment)
    : DialogFragment(),
    FriendNickNameProvider.Callback {
    private val friendNickNameProvider = FriendNickNameProvider(this)

    private lateinit var adapter: FriendNickNameListAdapter

    private val handler = Handler(Looper.getMainLooper())

    private val addedChips = HashSet<String>()

    private var selectedFriend: String = ""

    interface DataTransferListener {
        fun onDataTransfer(data: String)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setView(binding.root)
        setAdapter()

        friendNickNameProvider.getFriendNickName("")

        setEditTextView()

        setButton()

        return dialogBuilder.create()
    }


    private fun setAdapter() {
        adapter = FriendNickNameListAdapter { friend ->
            if (!addedChips.contains(friend.nickName)) {
                val chip = Chip(this.requireContext())
                chip.text = friend.nickName
                chip.isCloseIconVisible = true
                chip.setOnCloseIconClickListener {
                    binding.chipGroup.removeView(chip)
                    addedChips.remove(friend.nickName)
                }
                binding.chipGroup.addView(chip)
                addedChips.add(friend.nickName)
                selectedFriend = friend.nickName
            }
        }
        binding.recyclerView.adapter = adapter
    }

    private fun setEditTextView() {
        binding.textInputEditText.addTextChangedListener {
            val runnable = Runnable {
                friendNickNameProvider.getFriendNickName(it.toString())
            }
            handler.removeCallbacks(runnable)
            handler.postDelayed(runnable, 300)

        }
    }

    private fun setButton() {
        binding.cancelButton.setOnClickListener {
            dialog?.dismiss()
        }

        binding.okButton.setOnClickListener {
            dataTransferListener.onDataTransfer(selectedFriend)
            Log.d("tag", "selectedFriend is $selectedFriend")
            dialog?.dismiss()
        }
    }

    override fun loadFriendNickNameList(list: MutableList<Friend>) {
        //이부분에서 정보가 넘어오게되면 여기서 recyclerview를 업데이트 해줘야한다
        adapter.submitList(list)
        adapter.notifyDataSetChanged()
        binding.emptyTextView.isVisible = list.size == 0
    }

}