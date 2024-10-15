package com.example.tripick

import android.os.Bundle
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


class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var map: GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        return view
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        // 예시: 여행지에 마커 추가
        addMarker("여행지 제목", "여행지 상세", LatLng(37.5665, 126.978)) // 서울

        // 추가한 마커 클릭 리스너
        map.setOnMarkerClickListener { marker ->
            // 클릭 시 해당 여행 기록 보기 로직
            true
        }
    }

    private fun addMarker(title: String, snippet: String, location: LatLng) {
        map.addMarker(MarkerOptions().position(location).title(title).snippet(snippet))
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 10f))
    }
}
