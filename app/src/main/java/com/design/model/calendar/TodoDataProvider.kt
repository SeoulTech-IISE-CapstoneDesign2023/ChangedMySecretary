package com.design.model.calendar

import android.util.Log
import com.design.model.Todo
import com.design.util.FirebaseUtil

class TodoDataProvider(private val callback: Callback) {

    fun getTodoData(startDate: String, todoKey: String) {
        val splitDateArr = splitDate(startDate)
        val year = splitDateArr[0].trim()
        val month = splitDateArr[1].trim()
        val day = splitDateArr[2].trim()

        FirebaseUtil.todoDataBase.child(year).child(month).child(day).child(todoKey).get()
            .addOnSuccessListener {
                val todoData = it.getValue(Todo::class.java)
                todoData?.let { data ->
                    callback.loadTodoData(data)
                }
            }
            .addOnFailureListener { e ->
                Log.e("TodoDataProvider", "Firebase 데이터 가져오기 실패: ${e.message}", e)
            }
    }

    interface Callback {
        fun loadTodoData(data: Todo)
    }

    private fun splitDate(date: String): Array<String> {
        Log.e("splitDate", date)
        val splitText = date.split(" ")
        val resultDate: Array<String> = Array(3) { "" }
        resultDate[0] = splitText[0]  //year
        resultDate[1] = splitText[1]  //month
        resultDate[2] = splitText[2]  //day
        return resultDate
    }
}