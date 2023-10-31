package com.design.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.design.R
import com.design.databinding.ItemMainBinding
import com.design.model.alarm.AlarmItem
import java.text.SimpleDateFormat
import java.util.Locale

class MainListAdapter(
    private val onSharedClick: (AlarmItem) -> Unit,
    private val onDeleteClick: (AlarmItem) -> Unit,
    private val onTodoClick: (AlarmItem) -> Unit
) : ListAdapter<AlarmItem, MainListAdapter.ViewHolder>(diffUtil) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemMainBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    inner class ViewHolder(private val binding: ItemMainBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AlarmItem) {
            binding.todoTitleTextView.text = item.message
            binding.dateTextView.text = item.dateTime
            //type에 따라 애니메이션 변경
            when (item.type) {
                "CAR" -> {
                    setAnimation(binding, R.raw.car_animation)
                }

                "WALK" -> {
                    setAnimation(binding, R.raw.walking_animation)
                }

                "PUBLIC" -> {
                    setAnimation(binding, R.raw.bus_animation)
                }
            }
            val startTime =
                SimpleDateFormat("yyyyMMddHHmm", Locale.KOREA).parse(item.appointmentTime)
            binding.timeRemainingTextView.setEndTime(startTime.time)
            binding.timeRemainingTextView.setInterval(1000 * 60)

            binding.shareButton.setOnClickListener {
                onSharedClick(item)
            }

            binding.deleteButton.setOnClickListener {
                onDeleteClick(item)
            }

            binding.editTodoButton.setOnClickListener {
                onTodoClick(item)
            }
        }

    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<AlarmItem>() {
            override fun areItemsTheSame(oldItem: AlarmItem, newItem: AlarmItem): Boolean {
                return oldItem.notificationId == newItem.notificationId
            }

            override fun areContentsTheSame(oldItem: AlarmItem, newItem: AlarmItem): Boolean {
                return oldItem == newItem
            }

        }
    }

    private fun setAnimation(binding: ItemMainBinding, animation: Int) {
        binding.typeAnimationView.apply {
            setAnimation(animation)
            playAnimation()
            loop(true)
        }
    }

}