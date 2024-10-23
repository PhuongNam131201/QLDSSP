package com.example.qldssp.ViewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.qldssp.Model.CategoryModel
import com.example.qldssp.Model.ItemsModel
import com.example.qldssp.Model.SliderModel
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.ValueEventListener
import java.util.Locale.Category

class MainViewModel(): ViewModel() {
    private val firebaseDatabase = FirebaseDatabase.getInstance()
    private val itemsReference: DatabaseReference = firebaseDatabase.getReference("Items") // Tham chiếu đến node "Items"
    private val _banner = MutableLiveData<List<SliderModel>>()
    private val _categoty=MutableLiveData<MutableList<CategoryModel>>()
    private val _recommend=MutableLiveData<MutableList<ItemsModel>>()

    val banner : MutableLiveData<List<SliderModel>> = _banner
    val category:LiveData<MutableList<CategoryModel>> = _categoty
    val recommend: LiveData<MutableList<ItemsModel>> = _recommend




    fun loadRecommended(){
        val Ref=firebaseDatabase.getReference("Items")
        Ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val lists = mutableListOf<ItemsModel>()
                for (childSnapshot in snapshot.children){
                    val list = childSnapshot.getValue(ItemsModel::class.java)
                    if (list!=null){
                        lists.add(list)
                    }
                    _recommend.value=lists
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }
    fun loadCategory(){
        val Ref=firebaseDatabase.getReference("Category")
        Ref.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
               val list= mutableListOf<CategoryModel>()
                for (child in snapshot.children){
                    val data = child.getValue(CategoryModel::class.java)
                    if (data!=null){
                        list.add(data)
                    }
                }
                _categoty.value=list
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
    fun loadBanner(){
        val Ref=firebaseDatabase.getReference("Banner")
        Ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<SliderModel>()
                for (child in snapshot.children){
                    val  data = child.getValue(SliderModel::class.java)
                    if(data !=null){
                        list.add(data!!)
                    }
                }
                _banner.value=list
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private val _items = MutableLiveData<MutableList<ItemsModel>>(mutableListOf())
    val items: LiveData<MutableList<ItemsModel>> get() = _items

    fun addItem(newItem: ItemsModel) {
        val itemId = itemsReference.push().key // Tạo ID mới cho sản phẩm
        if (itemId != null) {
            // Lưu sản phẩm vào Firebase
            itemsReference.child(itemId).setValue(newItem).addOnSuccessListener {
                // Cập nhật LiveData sau khi lưu thành công
                val currentList = _items.value ?: mutableListOf()
                currentList.add(newItem)
                _items.value = currentList // Cập nhật LiveData
            }.addOnFailureListener { exception ->
                // Xử lý lỗi nếu cần thiết
                Log.e("Firebase", "Error saving item", exception)
            }
        }
    }
}