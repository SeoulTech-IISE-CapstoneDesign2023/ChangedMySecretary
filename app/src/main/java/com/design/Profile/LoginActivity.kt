package com.design.Profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.design.MainActivity
import com.design.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    val binding by lazy { ActivityLoginBinding.inflate(layoutInflater) }
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // 툴바 설정
        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)

        // 뒤로가기 버튼 활성화
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // 타이틀 제거
        supportActionBar?.setDisplayShowTitleEnabled(false)


        // Initialize Firebase Auth
        auth = Firebase.auth

        // 로그인 버튼 (로그인 과정)
        binding.SignInBtn.setOnClickListener {
            val email = binding.inputID.text.toString()
            val password = binding.inputPW.text.toString()

            if (email == "") {
                Toast.makeText(
                    baseContext, "아이디를 입력해주세요",
                    Toast.LENGTH_SHORT,
                ).show()
            } else if (password == "") {
                Toast.makeText(
                    baseContext, "비밀번호를 입력해주세요",
                    Toast.LENGTH_SHORT,
                ).show()
            } else {
                // 파이어베이스 로그인
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            val currentUser = auth.currentUser
                            val userId = currentUser?.uid ?: ""

                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish()

                        } else {
                            binding.inputPW.text?.clear()
                            // If sign in fails, display a message to the user.
                            Toast.makeText(
                                baseContext,
                                "아이디 비밀번호를 확인하세요",
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    }
            }
        }

        // 가입하기 버튼
        binding.SignUpBtn.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }


        binding.findPWBtn.setOnClickListener {
            val emailAddress = binding.inputID.text.toString()
            if (emailAddress == "") {
                Toast.makeText(baseContext, "이메일을 입력해주세요", Toast.LENGTH_SHORT).show()
            } else {
                Firebase.auth.sendPasswordResetEmail(emailAddress)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // 이메일 전송 성공
                            Toast.makeText(
                                baseContext, "비밀번호 변경 이메일이 전송되었습니다",
                                Toast.LENGTH_SHORT,
                            ).show()
                            Log.d("my_test", "Email sent")
                        } else {
                            // 이메일 전송 실패
                            val exception = task.exception
                            Log.e("my_test", "Email sending failed")
                            Toast.makeText(
                                baseContext, "이메일을 다시 확인해주세요",
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                    }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        // 뒤로가기 버튼 클릭 시 액티비티 종료
        finish()
        return true
    }
}