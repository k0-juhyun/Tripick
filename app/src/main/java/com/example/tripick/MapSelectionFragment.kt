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
        val initialLocation = LatLng(37.5665, 126.978) // 예시 좌표
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, 10f))

        // 클릭 시 마커 추가
        map.setOnMapClickListener { latLng ->
            showConfirmationDialog(latLng) // 다이얼로그 표시
        }
    }

    private fun showConfirmationDialog(latLng: LatLng) {
        AlertDialog.Builder(requireContext())
            .setTitle("위치 선택")
            .setMessage("이 위치를 선택하시겠습니까?")
            .setPositiveButton("예") { dialog, _ ->
                selectedLocation = latLng
                Toast.makeText(requireContext(), "위치가 선택되었습니다: $latLng", Toast.LENGTH_SHORT).show()

                // FragmentResult로 위치 전달
                val result = Bundle().apply {
                    putDouble("latitude", latLng.latitude)
                    putDouble("longitude", latLng.longitude)
                }
                setFragmentResult("requestKey", result)

                // TripRecordFragment로 전환
                val tripRecordFragment = TripRecordFragment()
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, tripRecordFragment)
                    .addToBackStack(null)
                    .commit()

                dialog.dismiss()
            }
            .setNegativeButton("아니오") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }
}
