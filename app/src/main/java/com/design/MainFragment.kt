package com.design

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.design.adapter.MainListAdapter
import com.design.databinding.FragmentMainBinding
import com.design.model.Todo
import com.design.model.alarm.AlarmItem
import com.design.util.AlarmUtil
import com.design.util.FirebaseUtil
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError

class MainFragment : Fragment() {

    private var binding: FragmentMainBinding? = null
    private lateinit var mainListAdapter: MainListAdapter
    private val alarmList = mutableListOf<AlarmItem>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return FragmentMainBinding.inflate(inflater, container, false).apply {
            binding = this
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //이곳에서 ui작동
        binding?.let { binding ->
            setTitleTextView(binding)

            setButton(binding)

            setRecyclerView(binding)
        }
    }

    private fun setRecyclerView(binding: FragmentMainBinding): ChildEventListener {
        mainListAdapter = MainListAdapter(
            onSharedClick = {
                //todo 친구에게 AlarmItem을 전송해줘야함 친구를 선택하는 창이 뜨고 그 창에서 친구에게 공유
            },
            onDeleteClick = {
                val notificationId = it.notificationId ?: ""
                //알람 데이터 삭제
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("알람 삭제")
                    .setMessage("알람을 삭제하시겠습니까?")
                    .setPositiveButton("예") { _, _ ->
                        AlarmUtil.deleteAlarm(notificationId.toInt(), requireContext())
                    }
                    .setNegativeButton("아니요") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            },
            onTodoClick = {
                val todoId = it.todoId
                val year = it.dateTime?.substring(0, 5)
                val month = it.dateTime?.substring(6, 9)
                val day = it.dateTime?.substring(10, 13)
                Log.e("click", "$year $month $day")
                //todokey를 가져오고 todo를 가져온다음 intent로 다 넘겨줘야함
                FirebaseUtil.todoDataBase.child(year!!).child(month!!).child(day!!).child(todoId!!)
                    .get()
                    .addOnSuccessListener { data ->
                        val todo = data.getValue(Todo::class.java)
                        val bundle = Bundle()
                        bundle.putParcelable("todo", todo)
                        bundle.putString("todoKey", todoId)
                        val intent = Intent(requireContext(), CreateActivity::class.java)
                        intent.putExtra("todo", todo)
                        intent.putExtra("todoKey", todoId)
                        intent.putExtra("startDate", todo?.stDate)
                        startActivity(intent)
                    }
            }
        )
        binding.alarmRecyclerView.adapter = mainListAdapter

        return FirebaseUtil.alarmDataBase.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val (notificationId, alarmItem) = alarmItemPair(snapshot)
                if (notificationId != null && !listContainNotificationId(notificationId)) {
                    alarmList.add(alarmItem)
                    alarmList.sortBy { alarmItem ->
                        alarmItem.notificationId
                    }
                }
                mainListAdapter.submitList(alarmList)
                mainListAdapter.notifyDataSetChanged()

                binding.emptyTextView.isVisible = mainListAdapter.itemCount == 0
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val data = snapshot.value as Map<*, *>
                Log.e("onChildChanged", data.toString())
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val (notificationId, alarmItem) = alarmItemPair(snapshot)
                alarmList.remove(alarmItem)
                mainListAdapter.notifyDataSetChanged()
                binding.emptyTextView.isVisible = mainListAdapter.itemCount == 0
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

    private fun setButton(binding: FragmentMainBinding) {
        binding.alarmTextView.setOnClickListener {
            val viewPager = requireActivity().findViewById<ViewPager2>(R.id.viewPager)
            viewPager.currentItem = 1
        }

        binding.todayTextView.setOnClickListener {
            val viewPager = requireActivity().findViewById<ViewPager2>(R.id.viewPager)
            viewPager.currentItem = 1
        }
    }

    private fun setTitleTextView(binding: FragmentMainBinding) {
        FirebaseUtil.userDataBase.child("user_info").get()
            .addOnSuccessListener {
                val value = it.getValue(User::class.java)
                binding.titleTextView.text = "${value?.nickname}님\n기분 좋은 하루 되세요"
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
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
            message = data["message"] as String?,
            todoId = data["todoId"] as String?
        )
        return Pair(notificationId, alarmItem)
    }

    private fun listContainNotificationId(notificationId: String): Boolean {
        return alarmList.any { alarmItem ->
            alarmItem.notificationId == notificationId
        }
    }
}