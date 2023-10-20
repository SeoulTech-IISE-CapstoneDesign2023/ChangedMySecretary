package com.design.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.design.databinding.ItemFavoriteBinding
import com.design.model.importance.Importance

class FavoriteListAdapter(private val onClick : (Importance) -> Unit) : ListAdapter<Importance, FavoriteListAdapter.ViewHolder>(diffUtil) {

    inner class ViewHolder(private val binding: ItemFavoriteBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Importance) {
            binding.titleTextView.text = item.title

            binding.addressTextView.text =
                if(item.place == "") "추억의 장소는 없습니다." else item.place
            if(item.endY =="0"){
                binding.arrowImageView.isVisible = false
            }

            binding.arrowImageView.setOnClickListener {
                onClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemFavoriteBinding.inflate(
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
        val diffUtil = object : DiffUtil.ItemCallback<Importance>() {
            override fun areItemsTheSame(oldItem: Importance, newItem: Importance): Boolean {
                return oldItem.todoId == newItem.todoId
            }

            override fun areContentsTheSame(oldItem: Importance, newItem: Importance): Boolean {
                return oldItem == newItem
            }

        }
    }


}