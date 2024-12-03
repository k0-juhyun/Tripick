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
        val view = inflater.inflate(R.layout.fragment_map_selection, container, false)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this) // 비동기 호출
        return view
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.map = googleMap

        // 초기 위치 설정
        val initialLocation = LatLng(37.5665, 126.978) // 서울
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, 10f))

        // 예시: 마커 추가
        map.addMarker(MarkerOptions().position(initialLocation).title("서울"))
    }

    private fun addMarker(title: String, snippet: String, location: LatLng) {
        map.addMarker(MarkerOptions().position(location).title(title).snippet(snippet))
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 10f))
    }
}
