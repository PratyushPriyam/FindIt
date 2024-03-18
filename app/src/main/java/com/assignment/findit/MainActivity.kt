package com.assignment.findit

import SellUploadClass
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var productList: ArrayList<SellUploadClass>
    private lateinit var adapter: ProductAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recview)
        recyclerView.layoutManager = LinearLayoutManager(this)
        productList = ArrayList()
        adapter = ProductAdapter(this, productList)
        recyclerView.adapter = adapter

        // Fetch products from Firebase
        fetchProducts()
    }

    private fun fetchProducts() {
        val database = FirebaseDatabase.getInstance()
        val reference = database.getReference("FindIt/allSold")

        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                productList.clear()
                for (dataSnapshot in snapshot.children) {
                    val product = dataSnapshot.getValue(SellUploadClass::class.java)!!
                    if (product.isBought == "no") {
                        productList.add(product)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database errors here
            }
        })
    }
}
