package com.example.tripick

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class TripRecordActivity : AppCompatActivity() {

    private lateinit var travelTitle: EditText
    private lateinit var travelDiary: EditText
    private lateinit var addImageButton: Button
    private lateinit var imageView: ImageView
    private lateinit var addLocationButton: Button
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val imageRequestCode = 1001
    private val locationRequestCode = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trip_record)

        travelTitle = findViewById(R.id.travelTitle)
        travelDiary = findViewById(R.id.travelDiary)
        addImageButton = findViewById(R.id.addImageButton)
        imageView = findViewById(R.id.imageView)
        addLocationButton = findViewById(R.id.addLocationButton)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        addImageButton.setOnClickListener {
            openGallery()
        }

        addLocationButton.setOnClickListener {
            checkLocationPermissionAndAddLocation()
        }
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, imageRequestCode)
    }

    private fun checkLocationPermissionAndAddLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // 권한이 이미 허용됨
            addLocationToMap()
        } else {
            // 권한 요청
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), locationRequestCode)
        }
    }

    private fun addLocationToMap() {
        try {
            // 현재 위치를 가져오고, 지도 페이지로 이동
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val intent = Intent(this, MapActivity::class.java)
                    intent.putExtra("latitude", location.latitude)
                    intent.putExtra("longitude", location.longitude)
                    startActivity(intent)
                } else {
                    // 위치 정보를 가져올 수 없는 경우 처리
                }
            }
        } catch (e: SecurityException) {
            // 권한이 없어서 발생한 예외 처리
            e.printStackTrace()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationRequestCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한이 허용됨
                addLocationToMap()
            } else {
                // 권한이 거부됨, 사용자에게 알림
                // 예를 들어, Toast를 사용하여 알릴 수 있습니다.
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == imageRequestCode && resultCode == RESULT_OK && data != null) {
            val imageUri: Uri? = data.data
            imageView.setImageURI(imageUri)
            imageView.visibility = ImageView.VISIBLE
        }
    }
}
