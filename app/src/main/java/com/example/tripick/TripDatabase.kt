package com.example.tripick

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class TripDatabase(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_NAME = "trip_database.db"
        private const val DATABASE_VERSION = 2 // 데이터베이스 버전 증가
        const val TABLE_TRIPS = "trips"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableSQL = ("CREATE TABLE $TABLE_TRIPS ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "title TEXT,"
                + "details TEXT,"
                + "location TEXT,"
                + "imageUri TEXT,"
                + "startDate TEXT," // 시작 날짜 열 추가
                + "endDate TEXT)"    // 종료 날짜 열 추가
                )
        db.execSQL(createTableSQL)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TRIPS")
        onCreate(db)
    }
}
