package com.example.tripick

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import java.util.Calendar

class EditTripFragment : Fragment() {
    private var tripId: Long = 0 // 수정할 여행 기록 ID
    private lateinit var editTextTitle: EditText
    private lateinit var editTextDetails: EditText
    private lateinit var editTextLocation: EditText // 위치 입력 필드
    private lateinit var buttonSelectDates: Button // 날짜 선택 버튼
    private lateinit var buttonSave: Button
    private lateinit var tripImage: ImageView
    private var selectedStartDate: String = ""
    private var selectedEndDate: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_trip, container, false)

        // UI 요소 초기화
        editTextTitle = view.findViewById(R.id.editTextTitle)
        editTextDetails = view.findViewById(R.id.editTextDetails)
        editTextLocation = view.findViewById(R.id.editTextLocation) // 위치 필드 초기화
        buttonSelectDates = view.findViewById(R.id.buttonSelectDates) // 날짜 선택 버튼 초기화
        buttonSave = view.findViewById(R.id.buttonSave)
        tripImage = view.findViewById(R.id.mainImage)

        // 여행 기록 로드
        loadTripDetails()

        // 날짜 선택 버튼 클릭 리스너
        buttonSelectDates.setOnClickListener {
            showStartDatePickerDialog()
        }

        // 저장 버튼 클릭 리스너
        buttonSave.setOnClickListener {
            saveTripRecord()
        }

        // 뒤로가기 버튼 클릭 리스너
        view.findViewById<Button>(R.id.buttonBack).setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        return view
    }

    private fun loadTripDetails() {
        val tripRepository = TripRepository(requireContext())
        val tripRecord = tripRepository.getTripById(tripId)

        // UI 업데이트
        editTextTitle.setText(tripRecord.title)
        editTextDetails.setText(tripRecord.details)
        editTextLocation.setText(tripRecord.location) // 위치 업데이트
        selectedStartDate = tripRecord.startDate // 시작 날짜 업데이트
        selectedEndDate = tripRecord.endDate // 종료 날짜 업데이트
        buttonSelectDates.text = "$selectedStartDate ~ $selectedEndDate" // 버튼 텍스트 업데이트
        // 이미지 설정 (예: Glide 또는 Picasso 등을 사용하여 이미지 로드)
        // tripImage.setImageURI(Uri.parse(tripRecord.imageUri))
    }

    private fun showStartDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            // 선택된 시작 날짜 포맷 설정
            selectedStartDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
            buttonSelectDates.text = "$selectedStartDate ~ 종료 날짜 선택" // 버튼 텍스트 업데이트
            showEndDatePickerDialog()
        }, year, month, day).show()
    }

    private fun showEndDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            // 선택된 종료 날짜 포맷 설정
            val selectedEndDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)

            // 날짜 비교
            if (isStartDateAfterEndDate(selectedStartDate, selectedEndDate)) {
                Toast.makeText(requireContext(), "종료 날짜는 시작 날짜 이후여야 합니다.", Toast.LENGTH_SHORT).show()
            } else {
                this.selectedEndDate = selectedEndDate // 종료 날짜 저장
                buttonSelectDates.text = "$selectedStartDate ~ $selectedEndDate" // 버튼 텍스트 업데이트
            }
        }, year, month, day).show()
    }

    private fun isStartDateAfterEndDate(startDate: String, endDate: String): Boolean {
        val startParts = startDate.split("-").map { it.toInt() }
        val endParts = endDate.split("-").map { it.toInt() }

        val startCalendar = Calendar.getInstance().apply {
            set(startParts[0], startParts[1] - 1, startParts[2]) // 월은 0부터 시작
        }
        val endCalendar = Calendar.getInstance().apply {
            set(endParts[0], endParts[1] - 1, endParts[2]) // 월은 0부터 시작
        }

        return startCalendar.after(endCalendar) // 시작 날짜가 종료 날짜 이후인지 확인
    }

    private fun saveTripRecord() {
        val title = editTextTitle.text.toString()
        val details = editTextDetails.text.toString()
        val location = editTextLocation.text.toString()

        // 수정된 여행 기록 생성
        val updatedTripRecord = TripRecord(
            id = tripId,
            title = title,
            details = details,
            location = location, // 수정된 위치 저장
            imageUri = "", // 필요시 이미지 URI도 수정
            startDate = selectedStartDate, // 수정된 시작 날짜 저장
            endDate = selectedEndDate // 수정된 종료 날짜 저장
        )

        val tripRepository = TripRepository(requireContext())
        tripRepository.updateTrip(updatedTripRecord) // 여행 기록 업데이트

        Toast.makeText(requireContext(), "여행 기록이 수정되었습니다.", Toast.LENGTH_SHORT).show()

        // 이전 화면으로 돌아가기
        requireActivity().supportFragmentManager.popBackStack()
    }

    // 여행 기록 ID 설정 메서드
    fun setTripId(id: Long) {
        tripId = id
    }
}
