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

        // 초기 위치 설정 (서울)
        val initialLocation = LatLng(37.5665, 126.978)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, 10f))

        // 여행 기록의 위치 가져오기
        loadTripLocations()
    }

    private fun loadTripLocations() {
        val tripRepository = TripRepository(requireContext())
        val tripRecords = tripRepository.getAllTrips() // 모든 여행 기록 가져오기

        for (trip in tripRecords) {
            // 위치가 유효한지 확인
            val locationParts = trip.location.split(",")
            if (locationParts.size == 2) {
                val latitude = locationParts[0].toDoubleOrNull()
                val longitude = locationParts[1].toDoubleOrNull()

                if (latitude != null && longitude != null) {
                    val location = LatLng(latitude, longitude)
                    addMarker(trip.title, trip.details, location) // 마커 추가
                }
            }
        }
    }

    private fun addMarker(title: String, snippet: String, location: LatLng) {
        map.addMarker(MarkerOptions().position(location).title(title).snippet(snippet))
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 10f)) // 마지막 마커 위치로 카메라 이동
    }
}
