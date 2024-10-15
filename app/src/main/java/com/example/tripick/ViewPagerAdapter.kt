package com.example.tripick

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 3 // 페이지 수

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> HomeFragment() // 홈 프래그먼트
            1 -> TripRecordFragment() // 여행 기록 추가 프래그먼트
            2 -> MapFragment() // 지도 프래그먼트
            else -> throw IllegalArgumentException("Invalid position")
        }
    }
}
