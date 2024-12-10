package com.example.tripick

import android.app.DatePickerDialog
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import java.util.Calendar

class EditTripFragment : Fragment() {
    private var tripId: Long = 0 // 수정할 여행 기록 ID
    private lateinit var editTextTitle: EditText
    private lateinit var editTextDetails: EditText
    private lateinit var buttonChangeLocation: Button
    private lateinit var buttonSelectDates: Button // 날짜 선택 버튼
    private lateinit var buttonSave: Button
    private lateinit var viewPager: ViewPager // ViewPager 초기화
    private lateinit var buttonEditImage: Button // 사진 수정 버튼
    private var selectedStartDate: String = ""
    private var selectedEndDate: String = ""
    private val REQUEST_IMAGE_PICK = 1001 // 이미지 선택 요청 코드
    private val selectedImageUris = mutableListOf<Uri>() // 선택된 이미지 URI 목록

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_trip, container, false)

        // UI 요소 초기화
        editTextTitle = view.findViewById(R.id.editTextTitle)
        editTextDetails = view.findViewById(R.id.editTextDetails)
        buttonSelectDates = view.findViewById(R.id.buttonSelectDates) // 날짜 선택 버튼 초기화
        buttonSave = view.findViewById(R.id.buttonSave)
        viewPager = view.findViewById(R.id.viewPager) // ViewPager 초기화
        buttonEditImage = view.findViewById(R.id.buttonEditImage) // 사진 수정 버튼 초기화
        buttonChangeLocation = view.findViewById(R.id.buttonChangeLocation)

        // 여행 기록 로드
        loadTripDetails()

        // 날짜 선택 버튼 클릭 리스너
        buttonSelectDates.setOnClickListener {
            showStartDatePickerDialog()
        }

        // 위치 변경 버튼 클릭 리스너
        view.findViewById<Button>(R.id.buttonChangeLocation).setOnClickListener {
            showMapSelection()
        }

        // 저장 버튼 클릭 리스너
        buttonSave.setOnClickListener {
            saveTripRecord()
        }

        // 사진 수정 버튼 클릭 리스너
        buttonEditImage.setOnClickListener {
            openImagePicker()
        }

        // 뒤로가기 버튼 클릭 리스너
        view.findViewById<ImageButton>(R.id.buttonBack).setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        return view
    }

    private fun showMapSelection() {
        val mapFragment = MapSelectionFragment()
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, mapFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun loadTripDetails() {
        val tripRepository = TripRepository(requireContext())
        val tripRecord = tripRepository.getTripById(tripId)

        // UI 업데이트 (null 체크 추가)
        editTextTitle.setText(tripRecord.title ?: "")
        editTextDetails.setText(tripRecord.details ?: "")
        buttonChangeLocation.setText(tripRecord.location ?: "")
        selectedStartDate = tripRecord.startDate ?: ""
        selectedEndDate = tripRecord.endDate ?: ""
        buttonSelectDates.text = "$selectedStartDate ~ $selectedEndDate" // 버튼 텍스트 업데이트

        // 이미지 설정
        selectedImageUris.clear() // 기존 이미지 URI 목록 초기화
        tripRecord.imageUri?.split(",")?.forEach { uriString ->
            selectedImageUris.add(Uri.parse(uriString.trim())) // URI 추가
        }
        setupImagePager(selectedImageUris.map { it.toString() }) // URI를 문자열 리스트로 변환하여 설정
    }

    private fun setupImagePager(imageUris: List<String>) {
        if (imageUris.isNotEmpty()) {
            val adapter = ImagePagerAdapter(imageUris) // String 리스트를 사용
            viewPager.adapter = adapter // ViewPager에 어댑터 설정
        }
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
        val location = buttonChangeLocation.text.toString()

        // 수정된 여행 기록 생성
        val updatedTripRecord = TripRecord(
            id = tripId,
            title = title,
            details = details,
            location = location, // 수정된 위치 저장
            imageUri = selectedImageUris.joinToString(",") { it.toString() }, // 선택된 이미지 URI를 문자열로 변환하여 저장
            startDate = selectedStartDate, // 수정된 시작 날짜 저장
            endDate = selectedEndDate // 수정된 종료 날짜 저장
        )

        val tripRepository = TripRepository(requireContext())
        tripRepository.updateTrip(updatedTripRecord) // 여행 기록 업데이트

        Toast.makeText(requireContext(), "여행 기록이 수정되었습니다.", Toast.LENGTH_SHORT).show()

        // 이전 화면으로 돌아가기
        requireActivity().supportFragmentManager.popBackStack()
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true) // 여러 장 선택 허용
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK) {
            selectedImageUris.clear() // 기존 이미지 URI 목록 초기화

            if (data?.clipData != null) { // 여러 이미지 선택
                val count = data.clipData!!.itemCount
                for (i in 0 until count) {
                    val uri = data.clipData!!.getItemAt(i).uri
                    selectedImageUris.add(uri) // 선택한 URI 추가
                }
            } else if (data?.data != null) { // 단일 이미지 선택
                selectedImageUris.add(data.data!!) // 선택한 URI 추가
            }

            // URI를 문자열 리스트로 변환하여 ViewPager에 설정
            setupImagePager(selectedImageUris.map { it.toString() }) // Uri를 String으로 변환하여 전달
        }
    }

    // 여행 기록 ID 설정 메서드
    fun setTripId(id: Long) {
        tripId = id
    }
}
