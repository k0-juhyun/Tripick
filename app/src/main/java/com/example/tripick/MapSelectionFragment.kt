package com.example.tripick

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import androidx.fragment.app.setFragmentResult

class MapSelectionFragment : Fragment(), OnMapReadyCallback {
    private lateinit var map: GoogleMap
    private var selectedLocation: LatLng? = null
    private lateinit var button: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map_selection, container, false)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // 버튼 초기화
        button = view.findViewById(R.id.selectLocationButton)
        button.setOnClickListener {
            if (selectedLocation == null) {
                Toast.makeText(requireContext(), "위치를 선택하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 선택한 위치를 FragmentResult로 설정
            val result = Bundle().apply {
                putString("location_title", "선택한 위치")
                putDouble("latitude", selectedLocation!!.latitude)
                putDouble("longitude", selectedLocation!!.longitude)
            }

            setFragmentResult("requestKey", result)

            // TripRecordFragment로 전환
            val tripRecordFragment = TripRecordFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, tripRecordFragment)
                .addToBackStack(null)
                .commit()
        }


        return view
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        // 초기 위치 설정
        val initialLocation = LatLng(-34.0, 151.0) // 예시 좌표
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, 10f))

        // 클릭 시 마커 추가
        map.setOnMapClickListener { latLng ->
            map.clear() // 기존 마커 제거
            selectedLocation = latLng // 새로운 위치 저장
            map.addMarker(MarkerOptions().position(latLng).title("선택한 위치"))

            // 버튼 보이기
            button.visibility = View.VISIBLE
        }

    }
}
