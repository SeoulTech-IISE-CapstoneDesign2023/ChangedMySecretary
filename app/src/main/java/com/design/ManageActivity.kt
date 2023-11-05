package com.design

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.design.databinding.ActivityManageBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

class ManageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManageBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        val user = auth.currentUser

        getUserNickname(user?.uid.toString()) { nickname ->
            binding.nameText.text = nickname
        }
        binding.emailText.text= user?.email

        val itemList = arrayOf("닉네임 변경하기", "비밀번호 변경하기", "로그아웃")
        val adapter = ArrayAdapter(this, android.R.layout.simple_expandable_list_item_1, itemList)

        val listView = findViewById<ListView>(R.id.manageListView)
        listView.adapter = adapter

        listView.setOnItemClickListener { parent, view, position, id ->
            when (position) {
                0 -> {
                    val intent =
                        Intent(this, ChangeNicknameActivity::class.java)
                    startActivity(intent)
                }
                1 -> {
                    showResetPasswordConfirmationDialog()
                }
                2 -> {
                    showLogoutConfirmationDialog()
                }
                else -> {
                }
            }
        }
    }
    override fun onResume() {
        super.onResume()

        auth = Firebase.auth
        val user = auth.currentUser

        getUserNickname(user?.uid.toString()) { nickname ->
            binding.nameText.text = nickname
        }

    }

    private fun showLogoutConfirmationDialog() {
        val builder = MaterialAlertDialogBuilder(this)
        builder.setTitle("로그아웃")
            .setMessage("로그아웃 하시겠습니까?")
            .setPositiveButton("예") { dialog, which ->
                FirebaseAuth.getInstance().signOut()
                finish()
                val intent =
                    Intent(this, IntroActivity::class.java)
                startActivity(intent)
                // 로그아웃 완료 메시지 또는 원하는 작업을 수행하세요.
                Toast.makeText(this, "로그아웃되었습니다.", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("아니오") { dialog, which ->
                // 아무 작업도 수행하지 않고 대화 상자를 닫습니다.
                dialog.dismiss()
            }
            .show()
    }
    fun getUserNickname(userId: String, onDataChangeCallback: (String?) -> Unit) {
        val userData = FirebaseDatabase.getInstance().getReference("user")
        userData.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    if (userId == snapshot.key) {
                        val nicknameValue = snapshot.child("user_info").child("nickname").value?.toString()
                        onDataChangeCallback(nicknameValue)
                        return // 원하는 데이터를 찾았으므로 루프를 종료
                    }
                }
                //해당 유저를 찾지 못한 경우
                onDataChangeCallback(null)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // 에러 처리 코드
                onDataChangeCallback(null) // 에러 발생 시 닉네임 null
            }
        })
    }

    fun resetPassword(email: String, onResetComplete: (Boolean) -> Unit) {
        Firebase.auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // 이메일 전송 성공
                    Log.d("my_test", "Email sent")
                    onResetComplete(true)
                } else {
                    // 이메일 전송 실패
                    val exception = task.exception
                    Log.e("my_test", "Email sending failed")
                    onResetComplete(false)
                }
            }
    }

    private fun showResetPasswordConfirmationDialog() {
        auth = Firebase.auth
        val user = auth.currentUser

        val builder = MaterialAlertDialogBuilder(this)

        builder.setTitle("비밀번호 변경")
            .setMessage("비밀번호 변경 이메일 전송과 함께 로그아웃됩니다. 실행하시겠습니까?")
            .setPositiveButton("예") { dialog, which ->

                resetPassword(user?.email.toString()) {success ->
                    Toast.makeText(this, "이메일을 확인해주세요", Toast.LENGTH_SHORT).show()
                }

                FirebaseAuth.getInstance().signOut()
                finish()

                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
            .setNegativeButton("아니오") { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }
}