package com.design.model.importance

import com.design.util.FirebaseUtil

class ImportanceProvider(private val callback: Callback) {
    fun getImportanceData(){
        FirebaseUtil.importanceDataBase.get()
            .addOnSuccessListener {
                val data = it.value as Map<String,Any>
                val dataList = data.values.toList()
                val importanceList = dataList.map { item ->
                    val itemData = item as Map<String, Any>
                    Importance(
                        endY = itemData["endY"] as Double? ?: 0.0,
                        endX = itemData["endX"] as Double? ?: 0.0,
                        place = itemData["place"] as String,
                        title = itemData["title"] as String,
                        todoId = itemData["todoId"] as String
                    )
                }
                callback.loadImportanceList(importanceList)
            }
            .addOnFailureListener {  }
    }

    interface Callback {
        fun loadImportanceList(list : List<Importance>)
    }
}