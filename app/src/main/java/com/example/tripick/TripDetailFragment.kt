package com.example.tripick

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.bumptech.glide.Glide

class TripDetailFragment : Fragment() {
    private var tripId: Long = 0 // 여행 기록 ID

    companion object {
        private const val ARG_TRIP_ID = "trip_id"

        fun newInstance(tripId: Long): TripDetailFragment {
            val fragment = TripDetailFragment()
            val args = Bundle()
            args.putLong(ARG_TRIP_ID, tripId)
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var viewPager: ViewPager // ViewPager 초기화
    private lateinit var tripTitle: TextView // 여행 제목
    private lateinit var tripStartDate: TextView // 시작 날짜
    private lateinit var tripEndDate: TextView // 종료 날짜
    private lateinit var tripDetails: TextView // 여행 일기
    private lateinit var tripLocation: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            tripId = it.getLong(ARG_TRIP_ID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_trip_detail, container, false)

        // UI 요소 초기화
        tripTitle = view.findViewById(R.id.tripTitle)
        tripStartDate = view.findViewById(R.id.tripStartDate) // 시작 날짜 TextView
        tripEndDate = view.findViewById(R.id.tripEndDate) // 종료 날짜 TextView
        tripDetails = view.findViewById(R.id.tripDetails)
        tripLocation = view.findViewById(R.id.tripLocation)
        val deleteButton: Button = view.findViewById(R.id.buttonDelete)
        val backButton: Button = view.findViewById(R.id.buttonBack)
        val editButton: Button = view.findViewById(R.id.buttonEdit)

        // ViewPager 초기화
        viewPager = view.findViewById(R.id.viewPager)

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

    fun loadTripDetails(view: View) {
        val tripRepository = TripRepository(requireContext())
        val tripRecord = tripRepository.getTripById(tripId) // 여행 기록 가져오기

        // UI 업데이트
        tripTitle.text = tripRecord.title
        tripStartDate.text = "${tripRecord.startDate} ~ " // 시작 날짜 표시
        tripEndDate.text = "${tripRecord.endDate}" // 종료 날짜 표시
        tripDetails.text = tripRecord.details
        tripLocation.text = "여행지 위치: ${tripRecord.location}" // 위치 정보 표시

        // 이미지 URI를 안전하게 처리
        val imageUris = tripRecord.imageUri?.split(",")?.map { it.trim() } ?: emptyList()
        setupViewPager(imageUris)
    }

    private fun setupViewPager(imageUris: List<String>) {
        if (imageUris.isNotEmpty()) {
            val adapter = ImagePagerAdapter(imageUris)
            viewPager.adapter = adapter
        }
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

    // 여행 기록 ID 설정 메서드
    fun setTripId(id: Long) {
        tripId = id
    }
}
