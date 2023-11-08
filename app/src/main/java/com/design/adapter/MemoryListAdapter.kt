package com.design.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.design.databinding.ItemMemoryBinding
import com.design.model.tag.Tag

class MemoryListAdapter(private val onClick : (Tag) -> Unit) : ListAdapter<Tag, MemoryListAdapter.ViewHolder>(diffUtil) {

    inner class ViewHolder(private val binding: ItemMemoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Tag) {
            binding.titleTextView.text = item.title
            binding.dateTextView.text = item.date
            binding.addressTextView.text =
                if(item.place == "") "추억의 장소는 없습니다." else item.place
            if(item.endY == 0.0){
                binding.arrowImageView.isVisible = false
            }

            binding.arrowImageView.setOnClickListener {
                onClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemMemoryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        return holder.bind(currentList[position])
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<Tag>() {
            override fun areItemsTheSame(oldItem: Tag, newItem: Tag): Boolean {
                return oldItem.todoId == newItem.todoId
            }

            override fun areContentsTheSame(oldItem: Tag, newItem: Tag): Boolean {
                return oldItem == newItem
            }

        }
    }


}