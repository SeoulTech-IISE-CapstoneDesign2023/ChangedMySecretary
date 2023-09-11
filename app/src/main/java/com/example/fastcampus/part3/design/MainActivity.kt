package com.example.fastcampus.part3.design

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import coil.load
import com.example.fastcampus.part3.design.databinding.ActivityMainBinding
import com.example.fastcampus.part3.design.model.ImageResponse
import com.example.fastcampus.part3.design.pagerAdapter.PagerAdapter
import com.google.android.material.tabs.TabLayoutMediator
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val tabArray = arrayOf("시간", "공간")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val pagerAdapter = PagerAdapter(supportFragmentManager, lifecycle)
        binding.viewPager.adapter = pagerAdapter
        with(binding) {
            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                if (position == 0) {
                    tab.icon = getDrawable(R.drawable.baseline_calendar_month_24)
                } else {
                    tab.icon = getDrawable(R.drawable.baseline_map_24)
                }
                tab.text = tabArray[position]
            }.attach()
        }
        loadImage()
        initFloatingButton()
    }

    private fun loadImage() {
        RetrofitManager.imageService.getRandomImage()
            .enqueue(object : Callback<ImageResponse>{
                override fun onResponse(
                    call: Call<ImageResponse>,
                    response: Response<ImageResponse>
                ) {
                    if (response.isSuccessful){
                        val image = response.body()
                        image?.let {
                            binding.imageView.apply {
                                setBackgroundColor(Color.parseColor(image.color))
                                load(image.urls.regular)
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<ImageResponse>, t: Throwable) {

                }

            })
    }

    private fun initFloatingButton() {
        binding.scrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            if (scrollY == 0) {
                binding.floatingActionButton.extend()
            } else {
                binding.floatingActionButton.shrink()
            }
        }
    }

}