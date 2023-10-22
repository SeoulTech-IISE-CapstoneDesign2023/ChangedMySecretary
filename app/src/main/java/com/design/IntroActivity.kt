package com.design

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.design.adapter.SlidePagerAdapter
import com.design.databinding.ActivityIntroBinding

class IntroActivity : AppCompatActivity() {

    val binding by lazy { ActivityIntroBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // 슬라이드 이미지 및 설명 텍스트 배열 정의
        val slideImages = intArrayOf(
            R.drawable.timetable,
            R.drawable.baseline_calendar_month_24,
            R.drawable.white_circle,
            R.drawable.baseline_alarm_on_24,
            R.drawable.baseline_alarm_off_24
        )

        val slideTexts = arrayOf(
            "설명 1",
            "설명 2",
            "설명 3",
            "설명 4",
            "설명 5"
        )

        // ViewPager와 PagerAdapter 초기화
        val viewPager = binding.introViewPager
        val adapter = SlidePagerAdapter(slideImages, slideTexts)
        viewPager.adapter = adapter

        binding.LoginPageBtn.setOnClickListener {
            val intent =
                Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        binding.SignUpBtn.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }
}