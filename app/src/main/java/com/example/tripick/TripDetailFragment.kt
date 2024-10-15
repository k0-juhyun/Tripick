package com.example.tripick

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide

class TripDetailFragment : Fragment() {
    private var tripId: Long = 0 // 여행 기록 ID
    private lateinit var mainImage: ImageView // 여기서 초기화하지 않음
    private val REQUEST_IMAGE_PICK = 1001 // 이미지 선택 요청 코드

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_trip_detail, container, false)

        // UI 요소 초기화
        val tripTitle: TextView = view.findViewById(R.id.tripTitle)
        val tripStartDate: TextView = view.findViewById(R.id.tripStartDate) // 시작 날짜 TextView
        val tripEndDate: TextView = view.findViewById(R.id.tripEndDate) // 종료 날짜 TextView
        val tripDetails: TextView = view.findViewById(R.id.tripDetails)
        val deleteButton: Button = view.findViewById(R.id.buttonDelete)
        val backButton: Button = view.findViewById(R.id.buttonBack)
        val editButton: Button = view.findViewById(R.id.buttonEdit)

        // mainImage 초기화
        mainImage = view.findViewById(R.id.mainImage)

        // 삭제 버튼 클릭 리스너 설정
        deleteButton.setOnClickListener {
            deleteTripRecord()
        }

        // 뒤로가기 버튼 클릭 리스너 설정
        backButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack() // 이전 Fragment로 돌아감
        }

        // 수정 버튼 클릭 리스너 설정
        editButton.setOnClickListener {
            editTripRecord() // 수정 기능 호출
        }

        // 여행 기록 데이터 로드
        loadTripDetails(view)

        return view
    }

    private fun loadTripDetails(view: View) {
        val tripRepository = TripRepository(requireContext())
        val tripRecord = tripRepository.getTripById(tripId) // 여행 기록 가져오기

        // UI 업데이트
        val tripTitle: TextView = view.findViewById(R.id.tripTitle)
        val tripStartDate: TextView = view.findViewById(R.id.tripStartDate) // 시작 날짜 TextView 추가
        val tripEndDate: TextView = view.findViewById(R.id.tripEndDate) // 종료 날짜 TextView 추가
        val tripDetails: TextView = view.findViewById(R.id.tripDetails)

        tripTitle.text = tripRecord.title
        tripStartDate.text = "시작 날짜: ${tripRecord.startDate}" // 시작 날짜 표시
        tripEndDate.text = "종료 날짜: ${tripRecord.endDate}" // 종료 날짜 표시
        tripDetails.text = tripRecord.details

        // 이미지 설정 (Glide 사용)
        Glide.with(requireContext())
            .load(tripRecord.imageUri) // tripRecord에 이미지 URI가 있다고 가정
            .into(mainImage)
    }

    private fun deleteTripRecord() {
        try {
            val tripRepository = TripRepository(requireContext())
            tripRepository.deleteTrip(tripId) // 데이터베이스에서 삭제

            // 홈 화면 갱신
            val homeFragment = requireActivity().supportFragmentManager.findFragmentById(R.id.fragment_container) as? HomeFragment
            homeFragment?.loadTrips() // 홈 화면 데이터 갱신

            // 프래그먼트 종료
            requireActivity().supportFragmentManager.popBackStack()
        } catch (e: Exception) {
            e.printStackTrace() // 로그로 오류 출력
        }
    }

    private fun editTripRecord() {
        // EditTripFragment로 이동
        val editTripFragment = EditTripFragment()
        editTripFragment.setTripId(tripId) // 여행 기록 ID 전달

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, editTripFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                // 선택한 이미지 URI를 ImageView에 설정
                mainImage.setImageURI(uri)

                // 선택한 이미지 URI를 TripRecord에 저장하거나 업데이트
                val tripRepository = TripRepository(requireContext())
                val tripRecord = tripRepository.getTripById(tripId)
                tripRecord.imageUri = uri.toString() // 이미지 URI 업데이트

                // 필요 시 업데이트된 tripRecord를 다시 저장
                tripRepository.updateTrip(tripRecord)
            }
        }
    }

    // 여행 기록 ID 설정 메서드
    fun setTripId(id: Long) {
        tripId = id
    }
}
