package com.example.tripick

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.viewpager2.widget.ViewPager2
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewPager = findViewById(R.id.viewPager)
        bottomNavigationView = findViewById(R.id.bottomNavigationView)

        setupViewPager()
        setupBottomNavigation()
    }

    private fun setupViewPager() {
        val adapter = object : RecyclerView.Adapter<CustomViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
                val layoutId = when (viewType) {
                    0 -> R.layout.activity_trip_record // 여행 기록 페이지
                    1 -> R.layout.activity_main // 홈 페이지
                    2 -> R.layout.activity_map // 지도 페이지
                    else -> throw IllegalArgumentException("Invalid view type")
                }
                val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
                return CustomViewHolder(view)
            }

            override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
                // 데이터 바인딩을 여기에 추가합니다.
            }

            override fun getItemCount(): Int = 3 // 페이지 수
        }

        viewPager.adapter = adapter

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                bottomNavigationView.menu.getItem(position).isChecked = true
            }
        })
    }

    private fun setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.menu_record -> viewPager.currentItem = 0 // 여행 기록 페이지
                R.id.menu_home -> viewPager.currentItem = 1 // 홈 페이지
                R.id.menu_map -> viewPager.currentItem = 2 // 지도 페이지
            }
            true
        }
    }
}

// CustomViewHolder 정의
class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    // 필요한 뷰를 초기화하고 바인딩하는 메소드를 추가합니다.
}
