package com.design.Profile

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.design.R
import com.design.adapter.SlidePagerAdapter
import com.design.databinding.ActivityIntroBinding

class IntroActivity : AppCompatActivity() {

    private lateinit var backPressedCallback: OnBackPressedCallback
    private val binding by lazy { ActivityIntroBinding.inflate(layoutInflater) }

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
            R.drawable.img_1,
            R.drawable.img_4,
            R.drawable.img_3,
            R.drawable.img_2,
        )

        val slideTexts = arrayOf("알람으로 늦지않게 도착하도록 도와드릴게요",
            "날짜별로 여러 일정들을 관리해드릴게요",
            "친구들과 공유해서 추억을 쌓아보아요",
            "일정을 생성할 때 이동 경로를 알려드릴게요"

        )

        // ViewPager와 PagerAdapter 초기화
        val viewPager = binding.introViewPager
        val adapter = SlidePagerAdapter(slideImages, slideTexts)
        viewPager.adapter = adapter

        binding.StartAppBtn.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}