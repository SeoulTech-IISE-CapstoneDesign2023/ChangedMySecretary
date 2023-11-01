package com.design.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.design.databinding.ItemNicknameBinding
import com.design.model.friend.Friend

class FriendNickNameListAdapter(private val onClick : (Friend) -> Unit) :
    ListAdapter<Friend, FriendNickNameListAdapter.ViewHolder>(diffUtil) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemNicknameBinding.inflate(
                LayoutInflater.from(parent.context),
                parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        return holder.bind(currentList[position])
    }

    inner class ViewHolder(private val binding: ItemNicknameBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Friend) {
            binding.nickNameTextView.text = item.nickName
            binding.root.setOnClickListener{
                onClick(item)
            }
        }
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<Friend>() {
            override fun areItemsTheSame(oldItem: Friend, newItem: Friend): Boolean {
                return oldItem.nickName == newItem.nickName
            }

            override fun areContentsTheSame(oldItem: Friend, newItem: Friend): Boolean {
                return oldItem === newItem
            }

        }
    }


}