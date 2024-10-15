package com.example.tripick

import android.os.Parcel
import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng

data class LatLngWrapper(var latitude: Double, var longitude: Double) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readDouble(),
        parcel.readDouble()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeDouble(latitude)
        parcel.writeDouble(longitude)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<LatLngWrapper> {
        override fun createFromParcel(parcel: Parcel): LatLngWrapper {
            return LatLngWrapper(parcel)
        }

        override fun newArray(size: Int): Array<LatLngWrapper?> {
            return arrayOfNulls(size)
        }

        // LatLng를 LatLngWrapper로 변환하는 메서드
        fun fromLatLng(latLng: LatLng): LatLngWrapper {
            return LatLngWrapper(latLng.latitude, latLng.longitude)
        }
    }

    // LatLngWrapper를 LatLng로 변환하는 메서드
    fun toLatLng(): LatLng {
        return LatLng(latitude, longitude)
    }
}
