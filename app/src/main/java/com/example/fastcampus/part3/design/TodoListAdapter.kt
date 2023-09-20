package com.example.fastcampus.part3.design

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.fastcampus.part3.design.databinding.ItemTodoBinding
import com.example.fastcampus.part3.design.model.Todo

class TodoListAdapter(
    private val todoList: ArrayList<Todo>)
    : RecyclerView.Adapter<TodoListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTodoBinding.inflate(LayoutInflater.from(parent.context))
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val todoEntity = todoList[position]
        holder.setTodoListUI(todoEntity, position)
    }

    override fun getItemCount(): Int {
        return todoList.size
    }

    inner class ViewHolder(private val binding: ItemTodoBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val root = binding.root
        fun setTodoListUI(todo: Todo, position: Int) {
            binding.todoTextView.text = todo.title
            binding.dateTextView.text = todo.stTime
        }

    }
}