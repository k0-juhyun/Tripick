package com.example.tripick

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.setFragmentResultListener
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.gms.maps.model.Marker
import java.util.Locale

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private val REQUEST_CODE = 100 // 권한 요청 코드

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map_selection, container, false)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this) // 비동기 호출
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestStoragePermission() // 권한 요청
    }

    // 권한 요청 메서드
    private fun requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_CODE)
        }
    }

    // 권한 요청 결과 처리 메서드
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한이 허용되었을 때의 행동
                Log.d("MapFragment", "Permission granted")
            } else {
                // 권한이 거부되었을 때의 행동
                Log.d("MapFragment", "Permission denied")
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.map = googleMap

        // 초기 위치 설정 (서울)
        val initialLocation = LatLng(37.5665, 126.978)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, 10f))

        // 여행 기록의 위치 가져오기
        loadTripLocations()

        // 선택된 위치 마커 추가 리스너
        setFragmentResultListener("requestKey") { _, bundle ->
            val latitude = bundle.getDouble("latitude")
            val longitude = bundle.getDouble("longitude")
            val location = LatLng(latitude, longitude)
            addMarker("선택된 위치", "여기에서 선택한 위치입니다.", location) // 선택된 위치에 마커 추가
        }

        // InfoWindow 커스터마이징
        map.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
            override fun getInfoWindow(marker: Marker): View? {
                return null // 기본 InfoWindow는 안 보이게 함
            }

            override fun getInfoContents(marker: Marker): View? {
                val view = layoutInflater.inflate(R.layout.info_window_layout, null)

                // 마커에 해당하는 여행 정보
                val tripRecord = marker.tag as? TripRecord
                if (tripRecord != null) {
                    val titleText = view.findViewById<TextView>(R.id.titleText)
                    val dateText = view.findViewById<TextView>(R.id.dateText)
                    val photoImage = view.findViewById<ImageView>(R.id.photoImage)

                    // 제목, 날짜, 이미지 설정
                    titleText.text = tripRecord.title
                    dateText.text = "${tripRecord.startDate} ~ ${tripRecord.endDate}"

                    // 첫 번째 이미지 URI를 가져와서 로드
                    tripRecord.imageUri?.let {
                        Glide.with(requireContext())
                            .load(it) // 이미지 URI를 로드
                            .override(200, 200) // 원하는 크기로 조정
                            .fitCenter() // 이미지를 ImageView에 맞게 축소
                            .error(R.drawable.ic_map) // 에러 이미지 설정 (실패 시)
                            .listener(object : RequestListener<Drawable> {
                                override fun onLoadFailed(
                                    e: GlideException?,
                                    model: Any?,
                                    target: Target<Drawable>?,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    Log.e("Glide", "Image load failed", e)
                                    return false
                                }

                                override fun onResourceReady(
                                    resource: Drawable?,
                                    model: Any?,
                                    target: Target<Drawable>?,
                                    dataSource: DataSource?,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    return false
                                }
                            })
                            .into(photoImage)
                    }
                }
                return view
            }
        })

        // 마커 클릭 시 InfoWindow만 열리게 하고, 이동은 InfoWindow 클릭 시만
        map.setOnMarkerClickListener { marker ->
            marker.showInfoWindow() // InfoWindow를 열기 위해 return false
            false // InfoWindow를 열기만 하고, 이동은 여기서 하지 않음
        }

        // InfoWindow 클릭 시 상세 페이지로 이동
        map.setOnInfoWindowClickListener { marker ->
            val tripRecord = marker.tag as? TripRecord
            tripRecord?.let {
                navigateToTripDetail(it.id)
            }
        }
    }

    private fun loadTripLocations() {
        val tripRepository = TripRepository(requireContext())
        val tripRecords = tripRepository.getAllTrips() // 모든 여행 기록 가져오기

        for (trip in tripRecords) {
            // 위치가 유효한지 확인 (주소일 경우)
            val location = trip.location
            val geocoder = Geocoder(requireContext(), Locale.getDefault())

            try {
                val addresses = geocoder.getFromLocationName(location, 1) // 주소로 좌표 검색
                if (addresses != null && addresses.isNotEmpty()) {
                    val latitude = addresses[0].latitude
                    val longitude = addresses[0].longitude
                    val latLng = LatLng(latitude, longitude)

                    // 마커 추가
                    val marker = map.addMarker(MarkerOptions().position(latLng).title(trip.title))
                    marker?.tag = trip // 마커에 여행 기록을 태그로 설정
                } else {
                    Log.e("MapFragment", "주소를 찾을 수 없습니다: $location")
                }
            } catch (e: Exception) {
                Log.e("MapFragment", "주소 변환 실패: $location", e)
            }
        }
    }

    private fun navigateToTripDetail(tripId: Long) {
        val detailFragment = TripDetailFragment.newInstance(tripId)
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, detailFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun addMarker(title: String, snippet: String, location: LatLng) {
        map.addMarker(MarkerOptions().position(location).title(title).snippet(snippet))
        // 카메라를 마지막 마커 위치로 이동
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 10f))
    }
}
