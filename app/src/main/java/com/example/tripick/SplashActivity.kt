package com.example.tripick

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)  // 스플래시 화면 레이아웃을 설정

        // 3초 후에 MainActivity로 전환
        Handler().postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent) // MainActivity로 이동
            finish()  // SplashActivity 종료, 뒤로 가기 방지
        }, 2000)  // 2초 대기
    }
}
