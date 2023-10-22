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

        val itemList = arrayOf("비밀번호 변경하기", "닉네임 변경하기")
        val adapter = ArrayAdapter(this, android.R.layout.simple_expandable_list_item_1, itemList)

        val listView = findViewById<ListView>(R.id.manageListView)
        listView.adapter = adapter

        listView.setOnItemClickListener { parent, view, position, id ->
            when (position) {
                0 -> {
                    // 첫 번째 항목에 대한 동작 수행
                    showResetPasswordConfirmationDialog()
                }
                1 -> {
                    // 두 번째 항목에 대한 동작 수행
                    binding.editNickname.visibility = View.VISIBLE
                }
                else -> {
                }
            }
        }

        binding.editNicknameBtn.setOnClickListener{
            saveNicknameIfNotExists(user?.uid.toString(), binding.newNickname.text.toString()) {
                    isSuccess ->
                if (isSuccess) {
                    Toast.makeText(baseContext, "닉네임이 성공적으로 변경되었습니다", Toast
                        .LENGTH_SHORT).show()
                    binding.nameText.text = binding.newNickname.text.toString()
                    binding.editNickname.visibility = View.GONE
                } else {
                    Toast.makeText(baseContext, "이미 존재하는 닉네임입니다", Toast.LENGTH_SHORT).show()
                }
            }
        }


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

    fun saveNicknameIfNotExists(myUid: String, nickname: String, onDataChangeCallback: (Boolean) -> Unit) {
        val userData = FirebaseDatabase.getInstance().getReference("user")

        userData.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var isNicknameAvailable = true // 닉네임 사용 가능 여부를 나타내는 플래그

                for (snapshot in dataSnapshot.children) {
                    val nicknameValue = snapshot.child("user_info").child("nickname").value.toString()
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