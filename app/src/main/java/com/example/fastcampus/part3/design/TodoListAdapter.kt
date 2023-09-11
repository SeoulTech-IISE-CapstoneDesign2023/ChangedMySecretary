package com.example.fastcampus.part3.design

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil.ItemCallback
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.fastcampus.part3.design.databinding.ItemTodoBinding
import com.example.fastcampus.part3.design.model.Todo
import com.example.fastcampus.part3.design.model.Type

class TodoListAdapter : ListAdapter<Todo, TodoListAdapter.ViewHolder>(diffUtil) {

    inner class ViewHolder(private val binding: ItemTodoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Todo) {
            when (item.type) {
                Type.COMPLETE -> {
                    binding.typeImageView.setBackgroundResource(R.drawable.baseline_mood_green_24)
                }

                Type.READY -> {
                    binding.typeImageView.setBackgroundResource(R.drawable.baseline_mood_24)
                }
            }
            binding.dateTextView.text = item.date
            binding.todoTextView.text = item.todo
        }

    }

    companion object {
        val diffUtil = object : ItemCallback<Todo>() {
            override fun areItemsTheSame(oldItem: Todo, newItem: Todo): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Todo, newItem: Todo): Boolean {
                return oldItem == newItem
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemTodoBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }
}