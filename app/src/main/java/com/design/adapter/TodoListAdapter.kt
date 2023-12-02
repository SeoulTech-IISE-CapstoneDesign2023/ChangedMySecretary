package com.design.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.design.Listener.OnImportanceClickListener
import com.design.Listener.OnItemLongClickListener
import com.design.Listener.OnItemShortClickListener
import com.design.R
import com.design.databinding.ItemTodoBinding
import com.design.model.Todo
import com.design.util.FirebaseUtil

class TodoListAdapter(
    private val todoList: ArrayList<Todo>,
    private val itemLongClicklistener: OnItemLongClickListener,
    private val itemShortClickListener: OnItemShortClickListener,
) : RecyclerView.Adapter<TodoListAdapter.ViewHolder>() {

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
        val todoEntity = todoList[position]
        holder.setTodoListUI(todoEntity, position)

        // 일정이 클릭되었을 때 리스너 함수 실행
        holder.root.setOnClickListener {
            itemShortClickListener.onShortClick(position)
            true
        }

        // 일정이 길게 클릭되었을 때 리스너 함수 실행
        holder.root.setOnLongClickListener {
            itemLongClicklistener.onLongClick(position)
            true
        }
    }

    override fun getItemCount(): Int {
        return todoList.size
    }

    inner class ViewHolder(private val binding: ItemTodoBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val root = binding.root
        fun setTodoListUI(todo: Todo, position: Int) {
            binding.todoTextView.text = todo.title
            binding.dateTextView.text = "${todo.date} ${todo.time}"
        }
    }
}