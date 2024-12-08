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
import android.app.AlertDialog
import androidx.fragment.app.setFragmentResult

class MapSelectionFragment : Fragment(), OnMapReadyCallback {
    private lateinit var map: GoogleMap
    private var selectedLocation: LatLng? = null
    private lateinit var button: Button
    private var selectedMarker: com.google.android.gms.maps.model.Marker? = null // 선택된 마커

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map_selection, container, false)
        // SupportMapFragment 초기화
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // 버튼 초기화
        button = view.findViewById(R.id.selectLocationButton)
        button.visibility = View.GONE // 처음에는 숨김

        // 버튼 클릭 리스너
        button.setOnClickListener {
            // 선택된 위치가 null인지 확인
            if (selectedLocation == null) {
                Toast.makeText(requireContext(), "위치를 선택하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 선택한 위치를 FragmentResult로 설정
            val result = Bundle().apply {
                putDouble("latitude", selectedLocation!!.latitude)
                putDouble("longitude", selectedLocation!!.longitude)
            }

            setFragmentResult("requestKey", result)

            requireActivity().supportFragmentManager.popBackStack()
        }

        return view
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        // 초기 위치 설정
        val initialLocation = LatLng(37.5665, 126.978) // 예시 좌표
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, 10f))

        // 클릭 시 마커 추가
        map.setOnMapClickListener { latLng ->
            addMarker(latLng) // 마커 추가
        }

        // 마커 클릭 리스너 설정
        map.setOnMarkerClickListener { marker ->
            selectedLocation = marker.position
            showConfirmationDialog(selectedLocation!!) // 다이얼로그 표시
            true // 마커 클릭 이벤트가 소비되었음을 나타냄
        }
    }

    private fun addMarker(location: LatLng) {
        // 기존 마커가 있으면 제거
        selectedMarker?.remove()

        // 새로운 마커 추가
        selectedMarker = map.addMarker(MarkerOptions().position(location).title("선택한 위치"))

        // 카메라를 선택한 위치로 이동
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 10f))
    }

    private fun showConfirmationDialog(latLng: LatLng) {
        AlertDialog.Builder(requireContext())
            .setTitle("위치 선택")
            .setMessage("이 위치를 선택하시겠습니까?")
            .setPositiveButton("예") { dialog, _ ->
                // 선택된 위치를 FragmentResult로 설정
                val result = Bundle().apply {
                    putDouble("latitude", latLng.latitude)
                    putDouble("longitude", latLng.longitude)
                }
                setFragmentResult("requestKey", result)

                Toast.makeText(requireContext(), "위치가 선택되었습니다: $latLng", Toast.LENGTH_SHORT).show()

                requireActivity().supportFragmentManager.popBackStack()
                dialog.dismiss()
            }
            .setNegativeButton("아니오") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }
}
