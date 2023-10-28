package com.design

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import com.design.adapter.SlidePagerAdapter
import com.design.databinding.ActivityIntroBinding

class IntroActivity : AppCompatActivity() {

    private lateinit var backPressedCallback: OnBackPressedCallback
    val binding by lazy { ActivityIntroBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // 뒤로가기 콜백 초기화
        backPressedCallback = object : OnBackPressedCallback(true) {
            var waitTime = 0L
            override fun handleOnBackPressed() {
                if (System.currentTimeMillis() - waitTime >= 1500) {
                    waitTime = System.currentTimeMillis()
                    Toast.makeText(baseContext, "뒤로가기 버튼을 한번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    finishAffinity()
                }
            }
        }
        // 뒤로가기 콜백 활성화
        onBackPressedDispatcher.addCallback(this, backPressedCallback)


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