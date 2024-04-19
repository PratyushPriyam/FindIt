package com.assignment.findit

import SellUploadClass // Assuming your product data class
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Donate : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var productList: ArrayList<SellUploadClass>
    private lateinit var adapter: DonateAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donate)

        recyclerView = findViewById(R.id.donateRecView) // Assuming ID for RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        productList = ArrayList()
        adapter = DonateAdapter(this, productList)
        recyclerView.adapter = adapter

        // Fetch products from Firebase
        fetchDonateProducts()
    }

    private fun fetchDonateProducts() {
        val database = FirebaseDatabase.getInstance()
        val reference = database.getReference("FindIt/allSold")

        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                productList.clear()
                for (dataSnapshot in snapshot.children) {
                    val product = dataSnapshot.getValue(SellUploadClass::class.java)!!

                    // Filter for products with price 0
                    if (product.price.toString() == "0") {
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
