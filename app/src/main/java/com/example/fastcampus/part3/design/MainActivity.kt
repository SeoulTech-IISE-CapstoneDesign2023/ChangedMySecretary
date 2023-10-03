package com.example.fastcampus.part3.design

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.content.res.AppCompatResources
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import coil.load
import com.example.fastcampus.part3.design.Friend.FriendListActivity
import com.example.fastcampus.part3.design.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    private val calendarFragment = CalendarFragment()
    private val fragmentList = listOf(CalendarFragment(), MapFragment())
    private val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager, lifecycle, fragmentList)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViewPager()

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
                    // 메뉴 항목 2을 클릭한 경우 처리할 코드
                    // 예: 다른 화면으로 이동
                    true
                }
                // 다른 메뉴 항목에 대한 처리 추가
                else -> false
            }
        }

        auth = Firebase.auth
        val user = auth.currentUser

        Log.d("my_log","${user?.email}")

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
            startActivity(Intent(this, CreateActivity::class.java))
        }

    }

    private fun initViewPager() {
        binding.viewPager.adapter = viewPagerAdapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            if (fragmentList[position] is CalendarFragment) {
                tab.text = "시간"
                tab.icon =
                    AppCompatResources.getDrawable(this, R.drawable.baseline_calendar_month_24)
            } else {
                tab.text = "공간"
                tab.icon = AppCompatResources.getDrawable(this, R.drawable.baseline_map_24)
            }
        }.attach()
    }


}