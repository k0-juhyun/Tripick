package com.example.tripick

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.location.Geocoder
import android.util.Log
import java.util.Locale
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import java.io.IOException
import androidx.core.content.ContextCompat
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.google.android.gms.maps.model.LatLng
import java.util.Calendar
import androidx.fragment.app.setFragmentResultListener

class TripRecordFragment : Fragment() {
    private lateinit var editTextTitle: EditText
    private lateinit var editTextDetails: EditText
    private lateinit var buttonSelectDates: Button
    private lateinit var buttonSelectLocation: Button
    private lateinit var buttonAddTrip: Button
    private lateinit var buttonAddImages: Button
    private lateinit var viewPagerImages: ViewPager // ViewPager 추가
    private lateinit var locationTextView: TextView
    private var selectedStartDate: String = ""
    private var selectedEndDate: String = ""
    private var selectedLocation: LatLng? = null // 선택한 위치 저장
    private lateinit var tripRepository: TripRepository
    private var selectedImageUris: MutableList<Uri> = mutableListOf() // 선택된 이미지 URI 저장

    companion object {
        const val REQUEST_CODE_IMAGE_PICK = 1
        const val REQUEST_LOCATION_PERMISSION = 2
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
        buttonAddImages = view.findViewById(R.id.buttonAddImages)
        viewPagerImages = view.findViewById(R.id.viewPagerImages) // ViewPager 초기화
        locationTextView = view.findViewById(R.id.locationTextView)

        tripRepository = TripRepository(requireContext())

        checkLocationPermission()

        buttonSelectDates.setOnClickListener {
            showStartDatePickerDialog()
        }

        buttonSelectLocation.setOnClickListener {
            showMapSelection()
        }

        buttonAddTrip.setOnClickListener {
            addTripRecord()
        }

        buttonAddImages.setOnClickListener {
            openGalleryForImageSelection()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setFragmentResultListener("requestKey") { requestKey, bundle ->
            val latitude = bundle.getDouble("latitude")
            val longitude = bundle.getDouble("longitude")

            // 위치 이름 업데이트
            updateLocationName(latitude, longitude)

            // Toast로 선택된 위치 확인
            Toast.makeText(requireContext(), "위치가 선택되었습니다: $latitude, $longitude", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateLocationName(latitude: Double, longitude: Double) {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        try {
            val addressList = geocoder.getFromLocation(latitude, longitude, 1)
            if (addressList != null && addressList.isNotEmpty()) {
                val address = addressList[0].getAddressLine(0) // 전체 주소
                locationTextView.text = "선택한 위치: $address"
            } else {
                locationTextView.text = "선택한 위치: 주소를 찾을 수 없습니다."
            }
        } catch (e: IOException) {
            e.printStackTrace()
            locationTextView.text = "선택한 위치: 주소를 찾을 수 없습니다."
        }
    }

    private fun showStartDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            selectedStartDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
            buttonSelectDates.text = "$selectedStartDate ~ 종료 날짜 선택"
            showEndDatePickerDialog()
        }, year, month, day).show()
    }

    private fun showEndDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
            val selectedEndDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)

            if (isStartDateAfterEndDate(selectedStartDate, selectedEndDate)) {
                Toast.makeText(requireContext(), "종료 날짜는 시작 날짜 이후여야 합니다.", Toast.LENGTH_SHORT).show()
            } else {
                this.selectedEndDate = selectedEndDate
                buttonSelectDates.text = "$selectedStartDate ~ $selectedEndDate"
            }
        }, year, month, day).show()
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            // 권한이 없을 경우 요청
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        } else {
            // 권한이 이미 허용된 경우
            // 필요한 작업 수행 (예: 지도 초기화)
        }
    }

    // 권한 요청 결과 처리
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // 권한이 허용되었을 경우
                // 필요한 작업 수행 (예: 지도 초기화)
            } else {
                // 권한이 거부되었을 경우
                Toast.makeText(requireContext(), "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }
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

        return startCalendar.after(endCalendar)
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
        val imageUris = selectedImageUris.joinToString(",") // 선택된 이미지 URI 설정

        Log.d("AddTripRecord", "Location: $location")

        val newTripRecord = TripRecord(
            id = 0, // 초기값 설정, 나중에 데이터베이스에서 ID를 가져올 것
            title = title,
            details = details,
            location = location,
            imageUri = imageUris,
            startDate = selectedStartDate,
            endDate = selectedEndDate // 종료 날짜 추가
        )

        // 여행 기록 추가 및 ID 가져오기
        val tripId = tripRepository.insertTrip(newTripRecord) // ID 반환 받기

        Toast.makeText(requireContext(), "여행 기록이 추가되었습니다.", Toast.LENGTH_SHORT).show()

        // 추가 후, 입력 필드 초기화
        editTextTitle.text.clear()
        editTextDetails.text.clear()
        buttonSelectDates.text = "날짜 선택하기"
        selectedLocation = null // 선택한 위치 초기화
        selectedImageUris.clear() // 선택한 이미지 URI 초기화

        // TripDetailFragment로 이동
        val detailFragment = TripDetailFragment.newInstance(tripId)
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, detailFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun openGalleryForImageSelection() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*" // 모든 이미지 파일을 선택
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true) // 여러 선택 허용
        startActivityForResult(intent, REQUEST_CODE_IMAGE_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_IMAGE_PICK) {
                data?.let {
                    if (it.clipData != null) {
                        val count = it.clipData!!.itemCount
                        for (i in 0 until count) {
                            if (selectedImageUris.size < 3) { // 최대 3개 이미지 선택
                                val imageUri = it.clipData!!.getItemAt(i).uri
                                imageUri?.let { uri -> // null 체크 후 추가
                                    selectedImageUris.add(uri)
                                }
                            } else {
                                Toast.makeText(requireContext(), "최대 3개의 이미지만 선택할 수 있습니다.", Toast.LENGTH_SHORT).show()
                                break
                            }
                        }
                    } else if (it.data != null) {
                        // 단일 이미지 선택
                        val imageUri = it.data
                        imageUri?.let { uri -> // null 체크 후 추가
                            selectedImageUris.add(uri)
                        }
                    }
                }
                // ViewPager 업데이트
                setupImagePager(selectedImageUris.map { it.toString() }) // URI를 String 리스트로 변환하여 설정
            }
        }
    }

    private fun setupImagePager(imageUris: List<String>) {
        val adapter = ImagePagerAdapter(imageUris)
        viewPagerImages.adapter = adapter // ViewPager에 어댑터 설정
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
