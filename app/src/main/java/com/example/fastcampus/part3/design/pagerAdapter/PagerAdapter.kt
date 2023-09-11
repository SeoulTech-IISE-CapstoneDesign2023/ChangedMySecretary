package com.example.fastcampus.part3.design.pagerAdapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.fastcampus.part3.design.MapFragment
import com.example.fastcampus.part3.design.TimeFragment

class PagerAdapter(fragmentManager : FragmentManager, lifeCycle : Lifecycle):FragmentStateAdapter(fragmentManager,lifeCycle) {

    override fun getItemCount(): Int {
        return NUM_TABS
    }

    override fun createFragment(position: Int): Fragment {
        when(position){
            0 -> return TimeFragment()
        }
        return MapFragment()
    }

    companion object {
        const val NUM_TABS = 2
    }
}