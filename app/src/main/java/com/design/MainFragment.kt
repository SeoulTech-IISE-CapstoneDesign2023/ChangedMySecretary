package com.design

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.design.databinding.FragmentMainBinding
import com.design.util.FirebaseUtil

class MainFragment:Fragment() {

    private var binding : FragmentMainBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return FragmentMainBinding.inflate(inflater,container,false).apply {
            binding = this
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //이곳에서 ui작동
        binding?.let {binding ->
            FirebaseUtil.userDataBase.child("user_info").get()
                .addOnSuccessListener {
                    val value = it.getValue(User::class.java)
                    binding.titleTextView.text = "${value?.nickname}님\n기분 좋은 하루 되세요"
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}