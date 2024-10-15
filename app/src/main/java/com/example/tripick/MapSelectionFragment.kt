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
            // selectedLocation이 null인지 확인
            if (selectedLocation == null) {
                Toast.makeText(requireContext(), "위치를 선택하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // MainActivity를 안전하게 참조
            val mainActivity = activity as? MainActivity
            if (mainActivity != null) {
                // 선택한 위치를 TripRecordFragment에 전달
                val tripRecordFragment = TripRecordFragment().apply {
                    arguments = Bundle().apply {
                        putString("title", mainActivity.getCurrentTitle())
                        putString("details", mainActivity.getCurrentDetails())
                        putString("startDate", mainActivity.getCurrentStartDate())
                        putString("endDate", mainActivity.getCurrentEndDate())
                        putParcelable("location", LatLngWrapper.fromLatLng(selectedLocation!!)) // LatLng를 Wrapper로 변환
                    }
                }

                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, tripRecordFragment)
                    .addToBackStack(null)
                    .commit()
            }
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
            // 기존 마커 제거
            map.clear()
            // 새로운 마커 추가
            selectedLocation = latLng
            map.addMarker(MarkerOptions().position(latLng).title("선택한 위치"))

            // 버튼 보이기
            button.visibility = View.VISIBLE
        }
    }
}
