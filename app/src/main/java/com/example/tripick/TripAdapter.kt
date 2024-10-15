package com.example.tripick

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide // Glide 라이브러리 사용

class TripAdapter(
    private val tripRecords: List<TripRecord>, // 여행 기록 리스트
    private val onItemClick: (TripRecord) -> Unit, // 항목 클릭 리스너
    private val onDeleteClick: (Long) -> Unit // 삭제 클릭 리스너
) : RecyclerView.Adapter<TripAdapter.TripViewHolder>() {

    inner class TripViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mainImage: ImageView = itemView.findViewById(R.id.mainImage)
        val tripDate: TextView = itemView.findViewById(R.id.tripDate) // 시작 및 종료 날짜
        val tripTitle: TextView = itemView.findViewById(R.id.tripTitle)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(tripRecords[position]) // 클릭 시 TripRecord 반환
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_trip_record, parent, false)

        // 최상단 블록인지 확인하여 높이 설정
        if (viewType == 0) {
            view.layoutParams.height = 800 // 최상단 블록 높이
        } else {
            view.layoutParams.height = 400 // 나머지 블록 높이
        }

        return TripViewHolder(view)
    }

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        val tripRecord = tripRecords[position]
        holder.tripTitle.text = tripRecord.title

        // 시작 날짜와 종료 날짜 표시
        holder.tripDate.text = "${tripRecord.startDate} ~ ${tripRecord.endDate}"

        // 이미지 설정 (Glide를 사용하여 이미지 로드)
        Glide.with(holder.itemView.context)
            .load(tripRecord.imageUri) // imageUri가 이미지의 URI라고 가정
            .into(holder.mainImage)
    }

    override fun getItemCount(): Int = tripRecords.size

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) 0 else 1 // 최상단 블록은 0, 나머지는 1로 구분
    }
}
