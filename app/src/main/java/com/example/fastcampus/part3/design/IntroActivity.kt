package com.example.fastcampus.part3.design

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.fastcampus.part3.design.databinding.ActivityIntroBinding

class IntroActivity : AppCompatActivity() {

    val binding by lazy { ActivityIntroBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

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