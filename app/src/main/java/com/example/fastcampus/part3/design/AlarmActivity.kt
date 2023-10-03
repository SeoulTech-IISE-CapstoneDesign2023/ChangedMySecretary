package com.example.fastcampus.part3.design

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.fastcampus.part3.design.adapter.AlarmListAdapter
import com.example.fastcampus.part3.design.databinding.ActivityAlarmBinding
import com.example.fastcampus.part3.design.model.Type
import com.example.fastcampus.part3.design.model.alarm.AlarmItem
import com.example.fastcampus.part3.design.util.AlarmUtil
import com.example.fastcampus.part3.design.util.FirebaseUtil
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError

class AlarmActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAlarmBinding

    private lateinit var alarmAdapter: AlarmListAdapter
    private val alarmList = mutableListOf<AlarmItem>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbarLayout)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        alarmAdapter = AlarmListAdapter {
            val notificationId = it.notificationId ?: ""
            //알람 데이터 삭제
            AlarmUtil.deleteAlarm(notificationId.toInt(), this)
            //todo 일정또한 삭제해야함
        }
        binding.recyclerView.adapter = alarmAdapter
        updateRecyclerView()
    }

    override fun onResume() {
        updateRecyclerView()
        super.onResume()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun updateRecyclerView() {
        FirebaseUtil.alarmDataBase.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val (notificationId, alarmItem) = alarmItemPair(snapshot)
                if (notificationId != null && !listContainNotificationId(notificationId)) {
                    alarmList.add(alarmItem)
                    alarmList.sortBy { alarmItem ->
                        alarmItem.notificationId
                    }
                }
                alarmAdapter.submitList(alarmList)
                alarmAdapter.notifyDataSetChanged()

                binding.emptyTextView.isVisible = alarmAdapter.itemCount == 0
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val data = snapshot.value as Map<*, *>
                Log.e("onChildChanged", data.toString())
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val (notificationId, alarmItem) = alarmItemPair(snapshot)
                alarmList.remove(alarmItem)
                alarmAdapter.notifyDataSetChanged()
                binding.emptyTextView.isVisible = alarmAdapter.itemCount == 0
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                val data = snapshot.value as Map<*, *>
                Log.e("onChildMoved", data.toString())
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("onCancelled", error.message)
            }

        })
    }

    private fun alarmItemPair(snapshot: DataSnapshot): Pair<String?, AlarmItem> {
        val data = snapshot.value as Map<*, *>
        val notificationId = data["notificationId"] as String?
        val alarmItem = AlarmItem(
            notificationId = notificationId,
            startLat = data["startLat"] as Double?,
            startLng = data["startLng"] as Double?,
            arrivalLat = data["arrivalLat"] as Double?,
            arrivalLng = data["arrivalLng"] as Double?,
            appointmentTime = data["appointmentTime"] as String?,
            type = data["type"] as String?,
            startPlace = data["startPlace"] as String?,
            arrivalPlace = data["arrivalPlace"] as String?,
            dateTime = data["dateTime"] as String?,
            message = data["message"] as String?
        )
        return Pair(notificationId, alarmItem)
    }

    private fun listContainNotificationId(notificationId: String): Boolean {
        return alarmList.any { alarmItem ->
            alarmItem.notificationId == notificationId
        }
    }


}