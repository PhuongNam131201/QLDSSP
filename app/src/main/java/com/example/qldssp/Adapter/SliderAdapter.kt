package com.example.qldssp.Adapter

import android.content.Context
import android.transition.Slide
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.qldssp.Model.SliderModel
import com.example.qldssp.R


class SliderAdapter(
    private var sliderItems: List<SliderModel>,
    private var viewPager2: ViewPager2
) : RecyclerView.Adapter<SliderAdapter.SliderViewholder>() {

    private val runnable = Runnable {
        viewPager2.setCurrentItem(0, true)
    }

    class SliderViewholder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageSlide)

        fun setImage(sliderModel: SliderModel) {
            Glide.with(itemView.context)
                .load(sliderModel.url)
                .into(imageView)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SliderAdapter.SliderViewholder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.slider_item_container, parent, false)
        return SliderViewholder(view)
    }

    override fun onBindViewHolder(holder: SliderViewholder, position: Int) {
        holder.setImage(sliderItems[position])

        // Nếu là item cuối cùng, đặt lại về slide đầu tiên
        if (position == sliderItems.size - 1) {
            viewPager2.post(runnable)
        }
    }

    override fun getItemCount(): Int {
        return sliderItems.size
    }
}
