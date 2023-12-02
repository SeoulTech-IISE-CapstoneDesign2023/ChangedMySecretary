package com.design

import android.content.DialogInterface
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.design.Listener.OnItemLongClickListener
import com.design.Listener.OnItemShortClickListener
import com.design.adapter.TodoListAdapter
import com.design.calendar.MySelectorDecorator
import com.design.calendar.OneDayDecorator
import com.design.calendar.SundayDecorator
import com.design.databinding.FragmentCalendarBinding
import com.design.model.Todo
import com.design.util.AlarmUtil
import com.design.util.FirebaseUtil
import com.design.util.Key.Companion.DB_CALENDAR
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.prolificinteractive.materialcalendarview.CalendarDay
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class CalendarFragment : Fragment(), OnItemLongClickListener, OnItemShortClickListener{

    private var binding: FragmentCalendarBinding? = null

    var todoKeys: ArrayList<String> = arrayListOf()     // 일정 ID (key) 리스트
    val todoList = arrayListOf<Todo>()                  // 일정 리스트
    private val adapter = TodoListAdapter(todoList, this, this)
    lateinit var user: String
    private var dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))

    // 선택한 날짜
    lateinit var clickedYear: String
    lateinit var clickedMonth: String
    lateinit var clickedDay: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return FragmentCalendarBinding.inflate(inflater, container, false).apply {
            binding = this
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun initView() {
        binding?.apply {
            // 유저 불러오기
            user = FirebaseAuth.getInstance().currentUser?.uid.toString()
            // calendar custom
            calendarView.addDecorators(
                SundayDecorator(),      // 일요일 빨간 글씨
                OneDayDecorator(),      // 오늘 날짜 색 다르게
                MySelectorDecorator(
                    requireActivity()
                )   // 선택한 날짜
            )
            recyclerView.adapter = adapter
            recyclerView.layoutManager = LinearLayoutManager(requireContext())

            // 오늘 날짜 받아오기
            val today = Calendar.getInstance()
            val todayYear = today[Calendar.YEAR]
            val todayMonth = today[Calendar.MONTH] + 1
            val todayDay = today[Calendar.DAY_OF_MONTH]
            val todayStr = String.format("%04d/%02d/%02d", todayYear, todayMonth, todayDay)
            // 시작 할 때 오늘 todolist 불러오기
            clickedDate(todayStr)

            //선택 날짜가 변경될 때 todolist 변경
            calendarView.setOnDateChangedListener { _, date, selected ->
                val year = date.year
                val month = date.month + 1
                val dayOfMonth = date.day
                dateStr = String.format("%04d/%02d/%02d", year, month, dayOfMonth)
                //날짜에 따른 todolist 불러오기
                clickedDate(dateStr)
            }

            todayView.setOnClickListener {
                // 오늘 날짜로 CalendarView를 설정
                calendarView.currentDate = CalendarDay.today()
                calendarView.clearSelection()
                calendarView.selectedDate = CalendarDay.today()
                clickedDate(todayStr)
            }
        }

        // 오늘 날짜 받아오기
        val today = Calendar.getInstance()
        val todayYear = today[Calendar.YEAR]
        val todayMonth = today[Calendar.MONTH] + 1
        val todayDay = today[Calendar.DAY_OF_MONTH]
        val todayStr = String.format("%04d/%02d/%02d", todayYear, todayMonth, todayDay)
        clickedDate(todayStr)
    }


    private fun clickedDate(date: String) {
        val clicked = splitDate(date)
        clickedYear = clicked[0].trim()
        clickedMonth = clicked[1].trim()
        clickedDay = clicked[2].trim()
        Firebase.database.reference.child(DB_CALENDAR)
            .child(user)
            .child(clickedYear + "년")
            .child(clickedMonth + "월")
            .child(clickedDay + "일")
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    Log.d("FirebaseData", "DataSnapshot: $dataSnapshot")
                    todoList.clear()
                    todoKeys.clear()
                    for (data in dataSnapshot.children) {
                        todoKeys.add(data.key!!)
                        todoList.add(data.getValue<Todo>()!!)
                    }
                    todoList.sortBy { it.time }
                    adapter.notifyDataSetChanged()
                    // 데이터를 받아온 후에 데이터가 비어 있는지 확인
                    if (todoList.isEmpty()) {
                        binding?.emptyTextView?.visibility = View.VISIBLE // 데이터가 비어 있으면 표시
                    } else {
                        binding?.emptyTextView?.visibility = View.GONE // 데이터가 있으면 숨김
                    }
                }
            })
    }

    // 날짜 함수
    private fun splitDate(date: String): Array<String> {
        val splitText = date.split("/")
        val resultDate: Array<String> = Array(3) { "" }
        resultDate[0] = splitText[0]  //year
        resultDate[1] = splitText[1]  //month
        resultDate[2] = splitText[2]  //day
        return resultDate
    }

    override fun onShortClick(position: Int) {
        Log.d("TimetableFragment", "$todoList")
        val todo = todoList[position] // 선택한 위치의 Todo객체를 가져옴
        val todoKey = todoList[position].todoId
        val startDate = todoList[position].date
        //Fragment로 데이터 전송
        val bundle = Bundle()
        bundle.putParcelable("todo", todo)
        bundle.putString("todoKey", todoKey)
        //Activity로 데이터 전송
        val intent = Intent(requireContext(), CreateActivity::class.java)
        intent.putExtra("todo", todo)
        intent.putExtra("todoKey", todoKey)
        intent.putExtra("startDate", startDate)
        startActivity(intent)
    }

    override fun onLongClick(position: Int) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("추억 삭제")
            .setMessage("추억을 삭제하시겠습니까?")
            .setPositiveButton("Yes",
                object : DialogInterface.OnClickListener {
                    override fun onClick(p0: DialogInterface?, p1: Int) {
                        deleteTodo(position)

                    }
                })
            .setNegativeButton("No", null)
            .show()
    }

    private fun deleteTodo(position: Int) {
        val todoKey = todoList[position].todoId
        // 일정 삭제시 알람도 같이 삭제
        val notificationId = todoList[position].notificationId ?: "0"
        FirebaseUtil.alarmDataBase.child(notificationId).removeValue()
        AlarmUtil.deleteAlarm(notificationId.toInt(), requireContext())
        todoList.removeAt(position)
        // 일정 삭제
        val deleteReference = Firebase.database.reference.child(DB_CALENDAR)
        deleteReference
            .child(user)
            .child(clickedYear + "년")
            .child(clickedMonth + "월")
            .child(clickedDay + "일")
            .child(todoKey)
            .removeValue().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "추억을 삭제했습니다.", Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }

}