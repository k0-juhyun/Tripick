package com.example.tripick

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase

class TripRepository(private val context: Context) {
    private val dbHelper = TripDatabase(context)

    // 여행 기록 추가
    fun insertTrip(tripRecord: TripRecord) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("title", tripRecord.title)
            put("details", tripRecord.details)
            put("location", tripRecord.location)
            put("imageUri", tripRecord.imageUri)
            put("startDate", tripRecord.startDate)
            put("endDate", tripRecord.endDate)
        }
        db.insert(TripDatabase.TABLE_TRIPS, null, values)
        db.close() // 데이터베이스 닫기
    }

    // 모든 여행 기록 가져오기
    fun getAllTrips(): List<TripRecord> {
        val trips = mutableListOf<TripRecord>()
        val db = dbHelper.readableDatabase
        val cursor: Cursor? = db.query(
            TripDatabase.TABLE_TRIPS,
            null,
            null,
            null,
            null,
            null,
            "startDate ASC"
        )

        cursor?.use {
            while (it.moveToNext()) {
                val id = it.getLong(it.getColumnIndexOrThrow("id"))
                val title = it.getString(it.getColumnIndexOrThrow("title"))
                val details = it.getString(it.getColumnIndexOrThrow("details"))
                val location = it.getString(it.getColumnIndexOrThrow("location"))
                val imageUri = it.getString(it.getColumnIndexOrThrow("imageUri"))
                val startDate = it.getString(it.getColumnIndexOrThrow("startDate"))
                val endDate = it.getString(it.getColumnIndexOrThrow("endDate"))

                trips.add(TripRecord(id, title, details, location, imageUri, startDate, endDate))
            }
        }
        db.close() // 데이터베이스 닫기
        return trips
    }

    // 특정 여행 기록 가져오기
    fun getTripById(tripId: Long): TripRecord {
        val db = dbHelper.readableDatabase
        val cursor: Cursor? = db.query(
            TripDatabase.TABLE_TRIPS,
            null,
            "id = ?",
            arrayOf(tripId.toString()),
            null,
            null,
            null
        )

        cursor?.use {
            if (it.moveToFirst()) {
                val id = it.getLong(it.getColumnIndexOrThrow("id"))
                val title = it.getString(it.getColumnIndexOrThrow("title"))
                val details = it.getString(it.getColumnIndexOrThrow("details"))
                val location = it.getString(it.getColumnIndexOrThrow("location"))
                val imageUri = it.getString(it.getColumnIndexOrThrow("imageUri"))
                val startDate = it.getString(it.getColumnIndexOrThrow("startDate"))
                val endDate = it.getString(it.getColumnIndexOrThrow("endDate"))

                return TripRecord(id, title, details, location, imageUri, startDate, endDate)
            }
        }

        db.close() // 데이터베이스 닫기
        throw Exception("Trip not found")
    }

    // 여행 기록 삭제
    fun deleteTrip(tripId: Long) {
        val db = dbHelper.writableDatabase
        db.delete(TripDatabase.TABLE_TRIPS, "id = ?", arrayOf(tripId.toString()))
        db.close() // 데이터베이스 닫기
    }

    // 여행 기록 업데이트
    fun updateTrip(tripRecord: TripRecord) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("title", tripRecord.title)
            put("details", tripRecord.details)
            put("location", tripRecord.location)
            put("imageUri", tripRecord.imageUri)
            put("startDate", tripRecord.startDate)
            put("endDate", tripRecord.endDate)
        }
        db.update(TripDatabase.TABLE_TRIPS, values, "id = ?", arrayOf(tripRecord.id.toString()))
        db.close() // 데이터베이스 닫기
    }
}
