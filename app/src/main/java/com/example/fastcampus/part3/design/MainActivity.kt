package com.example.fastcampus.part3.design

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import coil.load
import com.example.fastcampus.part3.design.databinding.ActivityMainBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val calendarFragment = CalendarFragment()
    private val fragmentList = listOf(CalendarFragment(), MapFragment())
    private val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager, lifecycle, fragmentList)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initViewPager()

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