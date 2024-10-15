package com.example.tripick

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
    private var tripRecordFragment: TripRecordFragment? = null // TripRecordFragment 인스턴스를 유지

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 초기 프래그먼트 설정
        if (savedInstanceState == null) {
            tripRecordFragment = TripRecordFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()
        }

        setupBottomNavigation()

        bottomNavigationView.selectedItemId = R.id.menu_home
    }

    private fun setupBottomNavigation() {
        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.menu_home -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, HomeFragment())
                        .commit()
                }
                R.id.menu_record -> {
                    // tripRecordFragment가 null이면 새로 생성
                    if (tripRecordFragment == null) {
                        tripRecordFragment = TripRecordFragment()
                    }
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, tripRecordFragment!!)
                        .commit()
                }
                R.id.menu_map -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, MapFragment())
                        .commit()
                }
            }
            true
        }
    }

    // 현재 입력된 제목, 세부사항, 날짜를 가져오는 메서드
    fun getCurrentTitle(): String? {
        return tripRecordFragment?.getCurrentTitle() // Fragment의 getter 메서드 호출
    }

    fun getCurrentDetails(): String? {
        return tripRecordFragment?.getCurrentDetails()
    }

    fun getCurrentStartDate(): String? {
        return tripRecordFragment?.getCurrentStartDate()
    }

    fun getCurrentEndDate(): String? {
        return tripRecordFragment?.getCurrentEndDate()
    }
}
