package com.example.tripick

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class TripImageAdapter(private val imageUris: List<String?>) : RecyclerView.Adapter<TripImageAdapter.TripImageViewHolder>() {

    class TripImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_trip_image, parent, false)
        return TripImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: TripImageViewHolder, position: Int) {
        val imageUri = imageUris[position]
        if (imageUri != null) {
            Glide.with(holder.imageView.context)
                .load(imageUri)
                .into(holder.imageView)
        } else {
            holder.imageView.setImageResource(R.drawable.ic_add) // 기본 이미지
        }
    }

    override fun getItemCount(): Int = imageUris.size
}
