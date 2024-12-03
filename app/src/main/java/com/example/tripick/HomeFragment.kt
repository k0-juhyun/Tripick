package com.example.tripick

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HomeFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var tripAdapter: TripAdapter
    private lateinit var textNoTrips: TextView // 여행 기록이 없을 때 사용할 TextView
    private lateinit var editTextSearch: EditText // 검색 바
    private val tripRecords: MutableList<TripRecord> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        textNoTrips = view.findViewById(R.id.textNoTrips) // TextView 초기화
        editTextSearch = view.findViewById(R.id.editTextSearch) // 검색 바 초기화

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        tripAdapter = TripAdapter(tripRecords, { tripRecord ->
            showTripDetails(tripRecord)
        }, { tripId ->
            deleteTripRecord(tripId)
        })

        recyclerView.adapter = tripAdapter

        // 데이터베이스에서 기존 여행 기록 불러오기
        loadTrips()

        // 검색 바에 텍스트 변경 리스너 추가
        editTextSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterTrips(s.toString()) // 입력된 텍스트에 따라 필터링
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        return view
    }

    public fun loadTrips() {
        val tripRepository = TripRepository(requireContext())
        val trips = tripRepository.getAllTrips() // 데이터베이스에서 모든 여행 기록 가져오기
        tripRecords.clear() // 기존 리스트 초기화
        tripRecords.addAll(trips.sortedBy { it.startDate }) // 시작 날짜 기준으로 정렬 후 데이터 추가

        if (tripRecords.isEmpty()) {
            textNoTrips.visibility = View.VISIBLE // 여행 기록이 없으면 메시지 보이기
            recyclerView.visibility = View.GONE // RecyclerView 숨기기
        } else {
            textNoTrips.visibility = View.GONE // 여행 기록이 있으면 메시지 숨기기
            recyclerView.visibility = View.VISIBLE // RecyclerView 보이기
            tripAdapter.notifyDataSetChanged() // RecyclerView 갱신
        }
    }

    private fun showTripDetails(tripRecord: TripRecord) {
        val tripDetailFragment = TripDetailFragment.newInstance(tripRecord.id) // 여행 기록 ID 전달
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, tripDetailFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun deleteTripRecord(tripId: Long) {
        try {
            val tripRepository = TripRepository(requireContext())
            tripRepository.deleteTrip(tripId) // 데이터베이스에서 삭제
            loadTrips() // 리스트 갱신
        } catch (e: Exception) {
            e.printStackTrace() // 로그로 오류 출력
        }
    }

    // 필터링 메서드 추가
    private fun filterTrips(query: String) {
        val filteredList = tripRecords.filter {
            it.title.contains(query, ignoreCase = true) ||
                    it.details.contains(query, ignoreCase = true)
        }
        tripAdapter.filterList(filteredList) // 어댑터에 필터링된 리스트 전달

        // 검색 결과가 없는 경우 메시지 표시
        if (filteredList.isEmpty()) {
            textNoTrips.visibility = View.VISIBLE
        } else {
            textNoTrips.visibility = View.GONE
        }
    }
}
