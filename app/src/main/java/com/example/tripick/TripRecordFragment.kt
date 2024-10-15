package com.example.tripick

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.model.LatLng
import java.util.Calendar

class TripRecordFragment : Fragment() {
    private lateinit var editTextTitle: EditText
    private lateinit var editTextDetails: EditText
    private lateinit var buttonSelectDates: Button
    private lateinit var buttonSelectLocation: Button
    private lateinit var buttonAddTrip: Button
    private lateinit var buttonAddLargeImage: Button
    private lateinit var buttonAddSmallImage1: Button
    private lateinit var buttonAddSmallImage2: Button
    private var selectedStartDate: String = ""
    private var selectedEndDate: String = ""
    private var selectedLocation: LatLng? = null // 선택한 위치 저장
    private lateinit var tripRepository: TripRepository
    private var selectedImageUri: Uri? = null // 선택된 이미지 URI 저장

    companion object {
        const val REQUEST_CODE_LARGE_IMAGE = 1
        const val REQUEST_CODE_SMALL_IMAGE_1 = 2
        const val REQUEST_CODE_SMALL_IMAGE_2 = 3
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_trip_record, container, false)

        editTextTitle = view.findViewById(R.id.editTextTitle)
        editTextDetails = view.findViewById(R.id.editTextDetails)
        buttonSelectDates = view.findViewById(R.id.buttonSelectDates)
        buttonSelectLocation = view.findViewById(R.id.buttonSelectLocation)
        buttonAddTrip = view.findViewById(R.id.buttonAddRecord)

        // 이미지 추가 버튼 초기화
        buttonAddLargeImage = view.findViewById(R.id.buttonAddLargeImage)
        buttonAddSmallImage1 = view.findViewById(R.id.buttonAddSmallImage1)
        buttonAddSmallImage2 = view.findViewById(R.id.buttonAddSmallImage2)

        tripRepository = TripRepository(requireContext())

        buttonSelectDates.setOnClickListener {
            showStartDatePickerDialog()
        }

        buttonSelectLocation.setOnClickListener {
            showMapSelection()
        }

        buttonAddTrip.setOnClickListener {
            addTripRecord()
        }

        // 큰 이미지 추가 버튼 클릭 리스너
        buttonAddLargeImage.setOnClickListener {
            openGalleryForImageSelection(REQUEST_CODE_LARGE_IMAGE)
        }

        // 작은 이미지 추가 버튼 클릭 리스너
        buttonAddSmallImage1.setOnClickListener {
            openGalleryForImageSelection(REQUEST_CODE_SMALL_IMAGE_1)
        }

        buttonAddSmallImage2.setOnClickListener {
            openGalleryForImageSelection(REQUEST_CODE_SMALL_IMAGE_2)
        }

        return view
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

    private fun showMapSelection() {
        val mapFragment = MapSelectionFragment()
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, mapFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun addTripRecord() {
        val title = editTextTitle.text.toString()
        val details = editTextDetails.text.toString()
        val location = selectedLocation?.let { "${it.latitude}, ${it.longitude}" } ?: "위치 없음"
        val imageUri = selectedImageUri?.toString() ?: "" // 선택된 이미지 URI 설정

        // 새로운 여행 기록 생성
        val newTripRecord = TripRecord(
            title = title,
            details = details,
            location = location,
            imageUri = imageUri,
            startDate = selectedStartDate,
            endDate = selectedEndDate // 종료 날짜 추가
        )

        tripRepository.insertTrip(newTripRecord)

        Toast.makeText(requireContext(), "여행 기록이 추가되었습니다.", Toast.LENGTH_SHORT).show()

        // 추가 후, 입력 필드 초기화
        editTextTitle.text.clear()
        editTextDetails.text.clear()
        buttonSelectDates.text = "날짜 선택하기"
        selectedLocation = null // 선택한 위치 초기화
        selectedImageUri = null // 선택한 이미지 URI 초기화
    }

    private fun openGalleryForImageSelection(requestCode: Int) {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*" // 모든 이미지 파일을 선택
        startActivityForResult(intent, requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data
            // 선택한 이미지 URI에 대한 추가 처리 (예: UI 업데이트)
            when (requestCode) {
                REQUEST_CODE_LARGE_IMAGE -> {
                    // 큰 이미지 추가 처리
                    Toast.makeText(requireContext(), "큰 이미지가 선택되었습니다.", Toast.LENGTH_SHORT).show()
                }
                REQUEST_CODE_SMALL_IMAGE_1 -> {
                    // 작은 이미지 1 추가 처리
                    Toast.makeText(requireContext(), "작은 이미지 1이 선택되었습니다.", Toast.LENGTH_SHORT).show()
                }
                REQUEST_CODE_SMALL_IMAGE_2 -> {
                    // 작은 이미지 2 추가 처리
                    Toast.makeText(requireContext(), "작은 이미지 2가 선택되었습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun getCurrentTitle(): String {
        return editTextTitle.text.toString()
    }

    fun getCurrentDetails(): String {
        return editTextDetails.text.toString()
    }

    fun getCurrentStartDate(): String {
        return selectedStartDate
    }

    fun getCurrentEndDate(): String {
        return selectedEndDate
    }
}
