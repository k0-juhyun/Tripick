package com.example.tripick

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide

class ImagePagerAdapter(private val imageUris: List<String>) : PagerAdapter() {

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        // FrameLayout을 inflate 한 후 그 안에 있는 ImageView를 찾아야 함
        val view = LayoutInflater.from(container.context)
            .inflate(R.layout.item_image_view, container, false)

        val imageView = view.findViewById<ImageView>(R.id.imageView) // ImageView 찾기

        Glide.with(container.context)
            .load(imageUris[position])
            .into(imageView)

        container.addView(view) // FrameLayout을 추가
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View) // FrameLayout을 제거
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun getCount(): Int = imageUris.size
}
