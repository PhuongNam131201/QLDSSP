package com.example.qldssp.Activity


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import com.example.qldssp.Adapter.CategoryAdapter
import com.example.qldssp.Adapter.RecommendationAdapter
import com.example.qldssp.Adapter.SliderAdapter
import com.example.qldssp.Model.ItemsModel
import com.example.qldssp.Model.SliderModel
import com.example.qldssp.R
import com.example.qldssp.ViewModel.MainViewModel
import com.example.qldssp.databinding.ActivityMainBinding
import com.google.firebase.database.FirebaseDatabase


class MainActivity : BaseActivity2() {
    private val viewModel = MainViewModel()
    private lateinit var binding: ActivityMainBinding
    private lateinit var recommendationAdapter: RecommendationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Khởi tạo adapter cho RecyclerView
        recommendationAdapter = RecommendationAdapter(mutableListOf())
        binding.viewRecommendation.layoutManager = GridLayoutManager(this, 2)
        binding.viewRecommendation.adapter = recommendationAdapter

        // Thiết lập sự kiện cho nút thêm sản phẩm
        binding.addBtn.setOnClickListener {

//
            showAddItemDialog()
        }

        initBanners()
        initCategory()
        initRecommended()
    }

    @SuppressLint("MissingInflatedId")
    private fun showAddItemDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.activity_edit_item, null)
        val imageUrlContainer = dialogView.findViewById<LinearLayout>(R.id.imageUrlContainer)
        val addImageButton = dialogView.findViewById<Button>(R.id.addImageButton)
        val imageUrlList = mutableListOf<String>()

        // Sự kiện thêm trường nhập URL ảnh
        addImageButton.setOnClickListener {
            val newImageUrlInput = EditText(this).apply {
                hint = "URL ảnh sản phẩm"
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            imageUrlContainer.addView(newImageUrlInput)

            // Cập nhật danh sách URL khi người dùng nhập
            newImageUrlInput.doOnTextChanged() { text, _, _, _ ->
                text?.let {
                    val url = it.toString()
                    if (url.isNotEmpty()) {
                        imageUrlList.add(url)
                    }
                }
            }
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Thêm sản phẩm mới")
            .setPositiveButton("Thêm") { _, _ ->
                val title = dialogView.findViewById<EditText>(R.id.titleInput).text.toString()
                val price = dialogView.findViewById<EditText>(R.id.priceInput).text.toString().toDoubleOrNull() ?: 0.0
                val rating = dialogView.findViewById<EditText>(R.id.ratingInput).text.toString().toDoubleOrNull() ?: 0.0
                val description = dialogView.findViewById<EditText>(R.id.desInput).text.toString()
//                val newItemId = myRef.push().key
                // Tạo đối tượng mới từ thông tin nhập vào
                val newItem = ItemsModel(
                    title = title,
                    price = price,
                    rating = rating,
                    picUrl = imageUrlList, // Thêm danh sách URL ảnh
                    description = description,

                )

                // Cập nhật sản phẩm vào danh sách của adapter
                recommendationAdapter.addItem(newItem)
                // Cập nhật lên Firebase
                val database = FirebaseDatabase.getInstance()
                val myRef = database.getReference("Items") // Thay "Items" bằng tên bạn muốn

                // Lấy khóa duy nhất cho sản phẩm
                val itemId = myRef.push().key
                itemId?.let {
                    myRef.child(it).setValue(newItem).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Sản phẩm đã được thêm!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this, "Thêm sản phẩm thất bại: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

            }
            .setNegativeButton("Hủy", null)
            .create()

        dialog.show()
    }



    private fun initBanners() {
        binding.progressBarBanner.visibility = View.VISIBLE
        viewModel.banner.observe(this, Observer {
            banners(it)
            binding.progressBarBanner.visibility = View.GONE
        })
        viewModel.loadBanner()
    }

    private fun banners(it: List<SliderModel>) {
        binding.viewPagerSlider.adapter = SliderAdapter(it, binding.viewPagerSlider)
        binding.viewPagerSlider.clipToPadding = false
        binding.viewPagerSlider.clipChildren = false
        binding.viewPagerSlider.offscreenPageLimit = 3
        if (binding.viewPagerSlider.childCount > 0) {
            binding.viewPagerSlider.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        }

        val compositePageTransformer = CompositePageTransformer().apply {
            addTransformer(MarginPageTransformer(40))
        }
        binding.viewPagerSlider.setPageTransformer(compositePageTransformer)
        if (it.size > 1) {
            binding.dotIndicator.visibility = View.VISIBLE
            binding.dotIndicator.attachTo(binding.viewPagerSlider)
        }
    }

    private fun initCategory() {
        binding.progressBarCategory.visibility = View.VISIBLE
        viewModel.category.observe(this, Observer {
            binding.viewCategory.layoutManager =
                LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            binding.viewCategory.adapter = CategoryAdapter(it)
            binding.progressBarCategory.visibility = View.GONE
        })
        viewModel.loadCategory()
    }

    private fun initRecommended() {
        binding.progressBarRecommendation.visibility = View.VISIBLE
        viewModel.recommend.observe(this, Observer {
            recommendationAdapter = RecommendationAdapter(it.toMutableList())
            binding.viewRecommendation.adapter = recommendationAdapter
            binding.progressBarRecommendation.visibility = View.GONE
        })
        viewModel.loadRecommended()
    }

}
