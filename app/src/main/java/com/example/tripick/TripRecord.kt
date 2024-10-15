package com.example.tripick

import android.os.Parcel
import android.os.Parcelable

data class TripRecord(
    val id: Long = 0, // 데이터베이스에서 자동 생성됨
    val title: String,
    val details: String,
    val location: String,
    var imageUri: String? = null, // 이미지 URI를 nullable로 변경
    val startDate: String, // 시작 날짜
    val endDate: String, // 종료 날짜 추가
    val imageResId: Int = R.drawable.ic_add, // 기본 큰 이미지 리소스
    val imageResId1: Int = R.drawable.ic_add, // 작은 이미지 1 기본 리소스
    val imageResId2: Int = R.drawable.ic_add, // 작은 이미지 2 기본 리소스
    val imageResId3: Int = R.drawable.ic_add  // 작은 이미지 3 기본 리소스
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString(), // 이미지 URI를 nullable로 읽기
        parcel.readString() ?: "", // 시작 날짜 읽기
        parcel.readString() ?: "", // 종료 날짜 읽기
        parcel.readInt(), // 큰 이미지 리소스
        parcel.readInt(), // 작은 이미지 1 리소스
        parcel.readInt(), // 작은 이미지 2 리소스
        parcel.readInt()  // 작은 이미지 3 리소스
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(title)
        parcel.writeString(details)
        parcel.writeString(location)
        parcel.writeString(imageUri) // 이미지 URI 쓰기
        parcel.writeString(startDate) // 시작 날짜 쓰기
        parcel.writeString(endDate) // 종료 날짜 쓰기
        parcel.writeInt(imageResId) // 큰 이미지 리소스
        parcel.writeInt(imageResId1) // 작은 이미지 1 리소스
        parcel.writeInt(imageResId2) // 작은 이미지 2 리소스
        parcel.writeInt(imageResId3) // 작은 이미지 3 리소스
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TripRecord> {
        override fun createFromParcel(parcel: Parcel): TripRecord {
            return TripRecord(parcel)
        }

        override fun newArray(size: Int): Array<TripRecord?> {
            return arrayOfNulls(size)
        }
    }
}
