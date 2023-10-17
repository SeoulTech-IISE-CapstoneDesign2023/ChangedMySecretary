package com.design.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.design.databinding.ItemAlarmBinding
import com.design.model.alarm.AlarmItem

class AlarmListAdapter(private val onClick : (AlarmItem) -> Unit) : ListAdapter<AlarmItem, AlarmListAdapter.ViewHolder>(diffUtil) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemAlarmBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }


    inner class ViewHolder(private val binding: ItemAlarmBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: AlarmItem) {
            binding.messageTextView.text = item.message
            binding.dateTimeTextView.text = item.dateTime
            binding.cancelButton.setOnClickListener {
                onClick(item)
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
}