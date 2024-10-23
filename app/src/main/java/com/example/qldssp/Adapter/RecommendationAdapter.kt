package com.example.qldssp.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.qldssp.Activity.DetailActivity
import com.example.qldssp.Model.ItemsModel
import com.example.qldssp.R
import com.example.qldssp.databinding.ViewholderRecommendBinding
import com.google.firebase.database.FirebaseDatabase

class RecommendationAdapter(private val items: MutableList<ItemsModel>) :
    RecyclerView.Adapter<RecommendationAdapter.Viewholder>() {

    private var context: Context? = null

    class Viewholder(val binding: ViewholderRecommendBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecommendationAdapter.Viewholder {
        context = parent.context
        val binding =
            ViewholderRecommendBinding.inflate(LayoutInflater.from(context), parent, false)
        return Viewholder(binding)
    }

    override fun onBindViewHolder(holder: Viewholder, position: Int) {
        if (items.isNotEmpty()) {
            val item = items[position]
            holder.binding.titleTxt.text = item.title
            holder.binding.priceTxt.text = item.price.toString()
            holder.binding.ratingTxt.text = item.rating.toString()

            Glide.with(holder.itemView.context)
            if (item.picUrl.isNotEmpty()) {
                Glide.with(holder.itemView.context)
                    .load(item.picUrl[0])
                    .apply(RequestOptions.centerCropTransform())
                    .into(holder.binding.pic)
            } else {
                // Load a default image if picUrl is empty

            }


            holder.itemView.setOnClickListener {
                val intent = Intent(holder.itemView.context, DetailActivity::class.java)
                intent.putExtra("object", item)
                holder.itemView.context.startActivity(intent)
            }
        }
    }


    override fun getItemCount(): Int = items.size

    // Hàm thêm sản phẩm vào danh sách và cập nhật giao diện
    fun addItem(item: ItemsModel) {
        items.add(item)
        notifyItemInserted(items.size - 1) // Cập nhật giao diện cho mục mới thêm
    }
    // Hàm xoá sản phẩm khỏi danh sách
    fun removeItem(position: Int) {
        if (position >= 0 && position < items.size) {
            items.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    // Hàm sửa sản phẩm trong danh sách và cập nhật giao diện
    fun updateItem(position: Int, updatedItem: ItemsModel) {
        if (position >= 0 && position < items.size) {
            items[position] = updatedItem
            notifyItemChanged(position) // Cập nhật giao diện cho mục bị thay đổi
        }
    }

}
