package com.example.fastcampus.part3.design

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import coil.load
import com.example.fastcampus.part3.design.databinding.FragmentTimeBinding
import com.example.fastcampus.part3.design.model.ImageResponse
import com.example.fastcampus.part3.design.model.Todo
import com.example.fastcampus.part3.design.model.Type
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class TimeFragment : Fragment() {

    private var binding: FragmentTimeBinding? = null

    private val adapter = TodoListAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return FragmentTimeBinding.inflate(inflater, container, false).apply {
            binding = this
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        adapter.submitList(mockData())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun initView() {
        binding?.recyclerView?.adapter = adapter
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