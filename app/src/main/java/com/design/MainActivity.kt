package com.design

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.drawerlayout.widget.DrawerLayout
import com.design.Friend.FriendListActivity
import com.design.adapter.ViewPagerAdapter
import com.design.databinding.ActivityMainBinding
import com.design.databinding.HeaderBinding
import com.design.util.TimeUtil
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var backPressedCallback: OnBackPressedCallback
    private lateinit var binding: ActivityMainBinding
    private lateinit var headerBinding: HeaderBinding
    private lateinit var auth: FirebaseAuth
    private var dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))
    private var fullDateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일"))

    private val fragmentList = listOf(MainFragment(), CalendarFragment(), MapFragment())
    private val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager, lifecycle, fragmentList)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // NavigationView에서 설정한 header 레이아웃을 참조
        val headerView = binding.navView.getHeaderView(0)

        // HeaderBinding을 사용하여 헤더 부분 초기화
        headerBinding = HeaderBinding.bind(headerView)

        initViewPager()
        setNotificationButton()

        // 뒤로가기 콜백 초기화
        backPressedCallback = object : OnBackPressedCallback(true) {
            var waitTime = 0L
            override fun handleOnBackPressed() {
                // 로그인이 완료된 경우 메인화면의 뒤로가기 -> 앱 종료
                if (auth.currentUser != null && auth.currentUser?.isEmailVerified == true) {
                    if (System.currentTimeMillis() - waitTime >= 1500) {
                        waitTime = System.currentTimeMillis()
                        Toast.makeText(baseContext, "뒤로가기 버튼을 한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        finishAffinity()
                    }
                }
            }
        }
        // 뒤로가기 콜백 활성화
        onBackPressedDispatcher.addCallback(this, backPressedCallback)

        auth = Firebase.auth
        val user = auth.currentUser

        // 사용자 이름 및 기타 정보 설정
        val userData = FirebaseDatabase.getInstance().getReference("user")
        userData.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    if (user?.uid == snapshot.key) {
                        val nicknameValue = snapshot.child("user_info").child("nickname").value
                            .toString()
                        headerBinding.userNameText.text = "$nicknameValue 님"
                    } else continue
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })

        binding.AppCompatImageView.setOnClickListener {
            val drawerLayout: DrawerLayout = binding.drawerLayout
            val navView = binding.navView
            drawerLayout.openDrawer(navView)
        }

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menuitem1 -> {
                    val intent =
                        Intent(this, FriendListActivity::class.java)
                    startActivity(intent)
                    true
                }

                R.id.menuitem2 -> {
                    val intent =
                        Intent(this, ManageActivity::class.java)
                    startActivity(intent)
                    true
                }
                // 다른 메뉴 항목에 대한 처리 추가
                else -> false
            }
        }


        Log.d("my_log", "${user?.email}")

        var count = 0
        if (user != null) {
            // 사용자가 로그인한 경우
            val userData =
                FirebaseDatabase
                    .getInstance()
                    .getReference("user")

            userData
                .addListenerForSingleValueEvent(object : ValueEventListener {

                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (snapshot in dataSnapshot.children) {
                            if (snapshot.key == user.uid) {
                                count = 1 // 로그인 되어있을 경우 1
                                break
                            } else continue
                        }

                        // 로그인 안된 경우 0 -> 로그인 화면으로 넘기기
                        if (count == 0) {
                            Firebase.auth.signOut()
                            val intent =
                                Intent(this@MainActivity, IntroActivity::class.java)
                            startActivity(intent)
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                    }
                })
        } else {
            // 사용자가 로그아웃한 경우 또는 인증 정보가 없는 경우
            Firebase.auth.signOut()
            val intent =
                Intent(this@MainActivity, IntroActivity::class.java)
            startActivity(intent)
        }

        // 오늘 날짜 받아오기
        val today = Calendar.getInstance()
        val todayYear = today[Calendar.YEAR]
        val todayMonth = today[Calendar.MONTH] + 1
        val todayDay = today[Calendar.DAY_OF_MONTH]
        val todayStr = String.format("%04d/%02d/%02d", todayYear, todayMonth, todayDay)

        binding.floatingActionButton.setOnClickListener {
            saveDate()
        }

    }

    override fun onResume() {
        super.onResume()

        auth = Firebase.auth
        val user = auth.currentUser
        // 사용자 이름 및 기타 정보 설정
        val userData = FirebaseDatabase.getInstance().getReference("user")
        userData.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    if (user?.uid == snapshot.key) {
                        val nicknameValue = snapshot.child("user_info").child("nickname").value
                            .toString()
                        headerBinding.userNameText.text = "$nicknameValue 님"
                    } else continue
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

    private fun setNotificationButton() {
        binding.notificationButton.setOnClickListener {
            startActivity(Intent(this, com.design.AlarmActivity::class.java))
        }
    }

    private fun initViewPager() {
        binding.viewPager.adapter = viewPagerAdapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            if (fragmentList[position] is MainFragment) {
                tab.text = "메인"
                tab.icon = AppCompatResources.getDrawable(this, R.drawable.baseline_home_24)
            } else if (fragmentList[position] is CalendarFragment) {
                tab.text = "시간"
                tab.icon =
                    AppCompatResources.getDrawable(this, R.drawable.baseline_calendar_month_24)
            } else if (fragmentList[position] is MapFragment) {
                tab.text = "공간"
                tab.icon = AppCompatResources.getDrawable(this, R.drawable.baseline_map_24)
            }
        }.attach()
    }

    private fun saveDate() {
        val intent = Intent(this, CreateActivity::class.java)
        Log.d("date", dateStr)
        val saveStr = splitDate(dateStr)
        val saveYear = saveStr[0].trim().toInt()
        val saveMonth = saveStr[1].trim().toInt() - 1
        val saveDay = saveStr[2].trim().toInt()
        val saveDayOfWeek = getDayOfWeek(saveYear, saveMonth, saveDay)
        Log.d("date", saveDayOfWeek)
        val saveDateStr = "$fullDateStr ($saveDayOfWeek)"
        Log.d("date", saveDateStr)
        intent.putExtra("startDate", saveDateStr)
        startActivity(intent)
    }

    private fun splitDate(date: String): Array<String> {
        Log.e("splitDate", date)
        val splitText = date.split("/")
        val resultDate: Array<String> = Array(3) { "" }
        resultDate[0] = splitText[0]  //year
        resultDate[1] = splitText[1]  //month
        resultDate[2] = splitText[2]  //day
        return resultDate
    }

    // 날짜로 요일 구하는 함수
    private fun getDayOfWeek(year: Int, month: Int, day: Int): String {
        val cal: Calendar = Calendar.getInstance()
        cal.set(year, month, day)

        return when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.SUNDAY -> "일"
            Calendar.MONDAY -> "월"
            Calendar.TUESDAY -> "화"
            Calendar.WEDNESDAY -> "수"
            Calendar.THURSDAY -> "목"
            Calendar.FRIDAY -> "금"
            Calendar.SATURDAY -> "토"
            else -> ""
        }
    }
}