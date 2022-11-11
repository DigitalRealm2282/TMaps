package com.tdi.tmaps.viewHolder
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.tdi.tmaps.R

class ImageAdapter internal constructor(var mContext: Context) : PagerAdapter() {
    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object` as ImageView
    }

    private val sliderImageId = intArrayOf(
        R.mipmap.bg,R.mipmap.greenleafbg
        ,R.mipmap.car
        ,R.mipmap.planegreenbg
    )

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val imageView = ImageView(mContext)
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        imageView.setImageResource(sliderImageId[position])
        (container as ViewPager).addView(imageView, 0)
        return imageView
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        (container as ViewPager).removeView(`object` as ImageView)
    }

    override fun getCount(): Int {
        return sliderImageId.size
    }
}