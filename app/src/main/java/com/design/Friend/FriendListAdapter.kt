package com.design.Friend

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.design.User
import com.design.databinding.MyfriendsViewBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class FriendListAdapter(
    private val context: FriendListActivity,
    private val myFriend: MutableList<User>,
    private val itemLongClicklistener: OnItemLongClickListener
) : RecyclerView.Adapter<FriendListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FriendListAdapter.ViewHolder {
        val binding =
            MyfriendsViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, context)
    }

    override fun onBindViewHolder(
        holder: FriendListAdapter.ViewHolder, position: Int
    ) {

        val friend = myFriend.get(position)
        holder.bind(friend)

        holder.binding.root.setOnLongClickListener {
            itemLongClicklistener.onLongClick(position)
            true
        }
    }

    override fun getItemCount(): Int {
        return myFriend.size
    }

    class ViewHolder(val binding: MyfriendsViewBinding, val context: FriendListActivity) :
        RecyclerView
        .ViewHolder(binding.root) {
        fun bind(friend: User) {
            binding.friendName.text = friend.nickname


            binding.imageCal.setOnClickListener {

                // 이곳에 친구 캘린더 보여주기 기능 필요
                // 단 동작은 불가 그냥 보여주기 (편집 권한 있으면 안됨)
                // 새로운 엑티비티로 정보 넘겨주면서 보여줘야 하나...

                Toast.makeText(context, "${friend.nickname} 친구 캘린더를 보여드려야하는데....", Toast
                    .LENGTH_SHORT).show()
            }

        }
    }
}