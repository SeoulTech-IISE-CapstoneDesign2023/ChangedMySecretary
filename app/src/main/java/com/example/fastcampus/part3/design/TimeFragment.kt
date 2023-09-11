package com.example.fastcampus.part3.design

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.fastcampus.part3.design.databinding.FragmentTimeBinding
import com.example.fastcampus.part3.design.model.Todo
import com.example.fastcampus.part3.design.model.Type


class TimeFragment : Fragment() {

    private lateinit var binding: FragmentTimeBinding

    private val adapter = TodoListAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTimeBinding.inflate(inflater, container, false)
        initView()
        adapter.submitList(mockData())
        return binding.root
    }

    private fun initView() {
        binding.recyclerView.adapter = adapter
    }

    private fun mockData(): List<Todo> {
        fun createDate(day: Int, dayOfWeek: String, time: String) = "${day}일($dayOfWeek) $time"

        val list = mutableListOf<Todo>().apply {
            add(
                Todo(
                    1,
                    createDate(3, "목", "15:00"),
                    "학교가기",
                    Type.COMPLETE,
                    true
                )
            )
            add(
                Todo(
                    2,
                    createDate(3, "목", "15:00"),
                    "학교가기",
                    Type.READY,
                    true
                )
            )
            add(
                Todo(
                    3,
                    createDate(3, "목", "15:00"),
                    "학교가기",
                    Type.COMPLETE,
                    false
                )
            )
            add(
                Todo(
                    4,
                    createDate(3, "목", "15:00"),
                    "학교가기",
                    Type.READY,
                    true
                )
            )
            add(
                Todo(
                    5,
                    createDate(3, "목", "15:00"),
                    "학교가기",
                    Type.COMPLETE,
                    false
                )
            )
            add(
                Todo(
                    6,
                    createDate(3, "목", "15:00"),
                    "학교가기",
                    Type.COMPLETE,
                    true
                )
            )
        }
        return list
    }

}