package com.design

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.design.databinding.MyfriendsSelectBinding
import com.design.util.Key.Companion.DB_USERS
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class InviteFriendAdapter(val User: MutableList<User>) : RecyclerView.Adapter<InviteFriendAdapter
.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val binding = MyfriendsSelectBinding.inflate(
            LayoutInflater.from(parent.context), parent,
            false
        )
        return ViewHolder(context, binding)
    }

    override fun getItemCount(): Int {
        return Math.min(40, User.size)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = User.get(position)
        holder.bind(user)
    }

    class ViewHolder(val context: Context, val binding: MyfriendsSelectBinding) : RecyclerView
    .ViewHolder(binding.root) {
        fun bind(user: User) {

            // friendView 레이아웃 조작
            binding.friendName.text = user.nickname
//
//            val auth = FirebaseAuth.getInstance().currentUser?.uid.toString()
//
//            Firebase.database.reference.child(DB_USERS).child(auth).child("friend_info")
//                .addValueEventListener(object : ValueEventListener {
//                    override fun onDataChange(snapshot: DataSnapshot) {
//                        for (userSnapshot in snapshot.child("friends").children) {
//                            val friend = userSnapshot.key.toString()
//
//                            if (user.uid == friend) {
//                                // 이미 친구인 경우 follow 버튼 비활성화
//                                binding.buttonShare.isEnabled = false
//                                break
//                            }
//                        }
//
//                        for (userSnapshot in snapshot.child("send_inv_req").children) {
//                            val friend = userSnapshot.key.toString()
//
//                            if (user.uid == friend) {
//                                // 이미 공유한 경우 공유 버튼 비활성화
//                                binding.buttonShare.isEnabled = false
//                                binding.buttonShare.text = "공유됨"
//                                break
//                            }
//                        }
//
//                    }
//
//                    override fun onCancelled(error: DatabaseError) {
//                    }
//                })

        }
    }

}
