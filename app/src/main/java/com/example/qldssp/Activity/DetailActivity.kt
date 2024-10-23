package com.example.qldssp.Activity

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import com.example.qldssp.Helper.ManagmentCart
import com.example.qldssp.Adapter.ColorAdapter
import com.example.qldssp.Adapter.SliderAdapter
import com.example.qldssp.Model.ItemsModel
import com.example.qldssp.Model.SliderModel
import com.example.qldssp.R
import com.example.qldssp.databinding.ActivityDetailBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding
    private lateinit var item: ItemsModel
    private var numberOrder = 1
    private lateinit var managmentCart: ManagmentCart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        managmentCart = ManagmentCart(this)

        getBundle()
        Banners()
        initColorList()
    }

    private fun initColorList() {
        val colorList = ArrayList<String>()
        if (!item.picUrl.isNullOrEmpty()) {
            for (imageUrl in item.picUrl) {
                colorList.add(imageUrl)
            }
        }
        binding.colorList.adapter = ColorAdapter(colorList)
        binding.colorList.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    }

    private fun Banners() {
        val sliderItems = ArrayList<SliderModel>()
        if (!item.picUrl.isNullOrEmpty()) {
            for (imageUrl in item.picUrl) {
                sliderItems.add(SliderModel(imageUrl))
            }
        }

        binding.slider.adapter = SliderAdapter(sliderItems, binding.slider)
        binding.slider.clipToPadding = false
        binding.slider.clipChildren = false
        binding.slider.offscreenPageLimit = 1

        if (binding.slider.childCount > 0) {
            binding.slider.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        }

        val compositePageTransformer = CompositePageTransformer().apply {
            addTransformer(MarginPageTransformer(40))
        }
        binding.slider.setPageTransformer(compositePageTransformer)

        if (sliderItems.size > 1) {
            binding.dotIndicator.visibility = View.VISIBLE
            binding.dotIndicator.attachTo(binding.slider)
        }
    }

    private fun getBundle() {
        item = intent.getParcelableExtra("object") ?: return

        binding.titleTxt.text = item.title
        binding.descriptionTxt.text = item.description
        binding.priceTxt.text = "${item.price}.000 VNĐ"
        binding.ratingTxt.text = "Đánh giá : ${item.rating}"

        // Gán sự kiện cho nút xóa
        binding.addToCartBtn.setOnClickListener {
            deleteItem()
        }

        // Gán sự kiện cho nút sửa
        binding.updateBtn.setOnClickListener {
            showUpdateDialog()
        }

        binding.backBtn.setOnClickListener { finish() }
    }

    private fun deleteItem() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Xác nhận")
        builder.setMessage("Bạn có chắc chắn muốn xóa sản phẩm này?")

        builder.setPositiveButton("Có") { dialog, which ->
            // Thực hiện xóa sản phẩm
            val database = FirebaseDatabase.getInstance()
            val myRef = database.getReference("Items")

            val itemName = item.title
            myRef.orderByChild("title").equalTo(itemName).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (childSnapshot in snapshot.children) {
                            childSnapshot.ref.removeValue().addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(this@DetailActivity, "Sản phẩm đã được xóa!", Toast.LENGTH_SHORT).show()
                                    finish()
                                } else {
                                    Toast.makeText(this@DetailActivity, "Không thể xóa sản phẩm: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    } else {
                        Toast.makeText(this@DetailActivity, "Sản phẩm không tồn tại!", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@DetailActivity, "Đã xảy ra lỗi: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        builder.setNegativeButton("Không") { dialog, which -> dialog.dismiss() }
        builder.show()
    }

    private fun showUpdateDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Cập nhật thông tin sản phẩm")

        // Inflate layout để nhập thông tin sản phẩm
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_update_item, null)
        builder.setView(dialogView)

        // Khởi tạo các view
        val titleInput = dialogView.findViewById<EditText>(R.id.editTitle)
        val descriptionInput = dialogView.findViewById<EditText>(R.id.editDescription)
        val priceInput = dialogView.findViewById<EditText>(R.id.editPrice)

        // Điền thông tin hiện tại vào input
        titleInput.setText(item.title)
        descriptionInput.setText(item.description)
        priceInput.setText(item.price.toString())

        builder.setPositiveButton("Cập nhật") { dialog, which ->
            val newTitle = titleInput.text.toString()
            val newDescription = descriptionInput.text.toString()
            val newPrice = priceInput.text.toString().toDoubleOrNull()

            if (newPrice != null) {
                updateItem(newTitle, newDescription, newPrice)
            } else {
                Toast.makeText(this@DetailActivity, "Giá không hợp lệ!", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Hủy") { dialog, which -> dialog.dismiss() }

        builder.show()
    }

    private fun updateItem(newTitle: String, newDescription: String, newPrice: Double?) {
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("Items")

        myRef.orderByChild("title").equalTo(item.title).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (childSnapshot in snapshot.children) {
                        val itemKey = childSnapshot.key
                        // Cập nhật thông tin sản phẩm
                        myRef.child(itemKey!!).child("title").setValue(newTitle)
                        myRef.child(itemKey).child("description").setValue(newDescription)
                        myRef.child(itemKey).child("price").setValue(newPrice).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this@DetailActivity, "Cập nhật thành công!", Toast.LENGTH_SHORT).show()
                                finish()
                            } else {
                                Toast.makeText(this@DetailActivity, "Cập nhật thất bại: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    Toast.makeText(this@DetailActivity, "Sản phẩm không tồn tại!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DetailActivity, "Đã xảy ra lỗi: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
