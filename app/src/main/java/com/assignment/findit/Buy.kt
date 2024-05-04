package com.assignment.findit

import SellUploadClass
import android.app.ActivityOptions
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Buy : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var productList: ArrayList<SellUploadClass>
    private lateinit var adapter: BoughtProductAdapter
    lateinit var logOutFab: AppCompatButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buy)

        val backBtn = findViewById<AppCompatButton>(R.id.backBtn)
        backBtn.setOnClickListener {
            finishAfterTransition()
        }

        recyclerView = findViewById(R.id.buyrecview)
        recyclerView.layoutManager = LinearLayoutManager(this)
        productList = ArrayList()
        adapter = BoughtProductAdapter(this, productList)
        recyclerView.adapter = adapter

        logOutFab = findViewById(R.id.logoutFab)
        logOutFab.setOnClickListener {
            // Inflate the custom layout for the dialog
            val view = LayoutInflater.from(this).inflate(R.layout.custom_layout_dialog, null)

            // Create an AlertDialog builder
            val builder = AlertDialog.Builder(this)

            // Set the custom view for the dialog
            builder.setView(view)

            // Find the buttons from the custom layout
            val yesButton = view.findViewById<Button>(R.id.yesButton)

            // Set positive (Yes) button click listener
            yesButton.setOnClickListener {
                // User clicked "Yes", proceed with logout
                val firebaseAuth = FirebaseAuth.getInstance()
                firebaseAuth.signOut()

                val profileIntent = Intent(this, Login::class.java)
                val options = ActivityOptions.makeSceneTransitionAnimation(this)
                startActivity(profileIntent, options.toBundle())
            }

            // Create and show the alert dialog
            val dialog = builder.create()
            dialog.show()
        }

        fetchBoughtProducts()
    }

    private fun fetchBoughtProducts() {
        val database = FirebaseDatabase.getInstance()
        val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid // Assuming user is logged in

        val reference = database.getReference("FindIt/allSold")
            .orderByChild("boughtBy")
            .equalTo(currentUserId)

        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                productList.clear()
                for (dataSnapshot in snapshot.children) {
                    val product = dataSnapshot.getValue(SellUploadClass::class.java)!!
                    if(product.isBought == "yes") {
                        productList.add(product)
                    }

                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database errors here
                Toast.makeText(applicationContext, "Error fetching bought products", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
