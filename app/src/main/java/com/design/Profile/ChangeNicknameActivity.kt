package com.design.Profile

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.design.databinding.ActivityChangeNicknameBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

class ChangeNicknameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChangeNicknameBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangeNicknameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        val user = auth.currentUser

        binding.editNicknameBtn.setOnClickListener {
            val inputNickname = binding.editNewNickname.text.toString();

            if (validateInput(inputNickname)) {
                saveNicknameIfNotExists(user?.uid.toString(), inputNickname) { isSuccess ->
                    if (isSuccess) {
                        Toast.makeText(
                            baseContext, "닉네임이 성공적으로 변경되었습니다", Toast
                                .LENGTH_SHORT
                        ).show()
                        finish()
                    } else {
                        Toast.makeText(baseContext, "이미 존재하는 닉네임입니다", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(baseContext, "닉네임이 유효하지 않습니다", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun validateInput(input: String): Boolean {
        // 정규표현식 패턴을 정의
        val pattern = "^[0-9a-zA-Z_-]{4,11}$" // 숫자, 영문, -, _ 중 4~11 글자
        // 정규표현식을 사용하여 입력값을 검증
        val regex = Regex(pattern)
        return regex.matches(input)
    }

    fun saveNicknameIfNotExists(
        myUid: String,
        nickname: String,
        onDataChangeCallback: (Boolean) -> Unit
    ) {
        val userData = FirebaseDatabase.getInstance().getReference("user")

        userData.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var isNicknameAvailable = true // 닉네임 사용 가능 여부를 나타내는 플래그

                for (snapshot in dataSnapshot.children) {
                    val nicknameValue =
                        snapshot.child("user_info").child("nickname").value.toString()
                    if (nickname == nicknameValue) {
                        isNicknameAvailable = false
                        break
                    }
                }
                if (isNicknameAvailable) {
                    // 닉네임 사용 가능
                    userData.child(myUid)
                        .child("user_info").child("nickname").setValue(nickname)
                    onDataChangeCallback(true) // 성공 콜백 호출
                } else {
                    // 닉네임 중복
                    onDataChangeCallback(false) // 실패 콜백 호출
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                onDataChangeCallback(false) // 데이터베이스 오류 발생 시 실패 콜백 호출
            }
        })
    }
}