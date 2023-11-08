package com.design.model.tag

import com.design.util.FirebaseUtil


class TagProvider(private val callback: Callback) {
    fun getShareData(){
        FirebaseUtil.tagDataBase.get()
            .addOnSuccessListener {
                val tagData = it.getValue(Tag::class.java)
                val shareList : ArrayList<Tag> = arrayListOf()
                shareList.add(tagData!!)
                shareList?.let { data ->
                    callback.loadShareList(data)
                }
            }
            .addOnFailureListener {  }
    }

    interface Callback {
        fun loadShareList(list : List<Tag>)
    }
}