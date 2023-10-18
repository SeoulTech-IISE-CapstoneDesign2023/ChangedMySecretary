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
    private val onImportanceClickListener: OnImportanceClickListener
) : RecyclerView.Adapter<TodoListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemTodoBinding.inflate(LayoutInflater.from(parent.context),parent,false))
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
            binding.dateTextView.text = "${todo.stDate} ${todo.stTime}"
            binding.importanceView.apply {
                if (todo.importance == false) {
                    setImageResource(R.drawable.baseline_gray_star_24)
                } else setImageResource(R.drawable.baseline_star_24)
                setOnClickListener {
                    if (todo.importance == true) {
                        //즐찾이 되어있는데 클릭했을 때
                        //즐찾을 해제해주기
                        onImportanceClickListener.onClick(position,false) //todo data update
                        setImageResource(R.drawable.baseline_gray_star_24)
                        //데이터 삭제해주기
                        FirebaseUtil.importanceDataBase.child(todo.todoId).removeValue()
                    } else {
                        //즐찾 추가
                        onImportanceClickListener.onClick(position,true)//todo data update
                        setImageResource(R.drawable.baseline_star_24)
                        //데이터추가해주기
                        val importance = mutableMapOf<String, Any>()
                        importance["endX"] = todo.arrivalLng ?: 0.0
                        importance["endY"] = todo.arrivalLat ?: 0.0
                        importance["todoId"] = todo.todoId
                        importance["place"] = todo.arrivePlace ?: ""
                        importance["title"] = todo.title
                        FirebaseUtil.importanceDataBase.child(todo.todoId).updateChildren(importance)
                    }
                }
            }
        }
    }
}