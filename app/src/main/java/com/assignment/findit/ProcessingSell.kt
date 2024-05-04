package com.assignment.findit

import SellUploadClass
import androidx.appcompat.app.AppCompatActivity
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProcessingSell : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var productList: ArrayList<SellUploadClass>
    private lateinit var adapter: ProcessingAdapter

    private val LOCATION_PERMISSION_REQUEST_CODE = 101 // Request code for location permissions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_processing_sell)

        recyclerView = findViewById(R.id.processrecview) // Assuming ID for recycler view
        recyclerView.layoutManager = LinearLayoutManager(this)
        productList = ArrayList()
        adapter = ProcessingAdapter(this, productList)
        recyclerView.adapter = adapter

        // Get Firebase Database instance
        val database = FirebaseDatabase.getInstance()

        val backBtn = findViewById<AppCompatButton>(R.id.backBtn)
        backBtn.setOnClickListener {
            finishAfterTransition()
        }

        // Fetch pending products
        val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
        val reference = database.getReference("FindIt/allSold")
            .orderByChild("sellerId") // Query for products based on seller ID
            .equalTo(currentUserId)

        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                productList.clear()
                for (dataSnapshot in snapshot.children) {
                    val product = dataSnapshot.getValue(SellUploadClass::class.java)!!

                    // Filter for pending products only
                    if (product.isBought == "pending") {
                        productList.add(product)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database errors
            }
        })
    }

    // Handle location permission request result (optional)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, handle click event again
                adapter.notifyDataSetChanged() // Refresh adapter to trigger click handling
            } else {
                Toast.makeText(this, "Location permission is required!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
