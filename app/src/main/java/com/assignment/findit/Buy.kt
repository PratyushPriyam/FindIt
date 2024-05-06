package com.assignment.findit

import SellUploadClass
import android.app.ActivityOptions
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.transition.Slide
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
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
        window.enterTransition = Slide(Gravity.END)
        window.exitTransition = Slide(Gravity.START)
        setContentView(R.layout.activity_buy)

        val backBtn = findViewById<AppCompatButton>(R.id.backBtn)
        backBtn.setOnClickListener {
            finishAfterTransition()
        }

        val floatingActionButton2 = findViewById<FloatingActionButton>(R.id.floatingActionButton2)
        floatingActionButton2.setOnClickListener { startActivity(Intent(this, Profile::class.java)) }

        recyclerView = findViewById(R.id.buyrecview)
        recyclerView.layoutManager = LinearLayoutManager(this)
        productList = ArrayList()
        adapter = BoughtProductAdapter(this, productList)
        recyclerView.adapter = adapter

        logOutFab = findViewById(R.id.logoutFab)
        logOutFab.setOnClickListener {
            val view = LayoutInflater.from(this).inflate(R.layout.custom_layout_dialog, null)

            val builder = AlertDialog.Builder(this)

            builder.setView(view)

            val yesButton = view.findViewById<Button>(R.id.yesButton)

            yesButton.setOnClickListener {
                val firebaseAuth = FirebaseAuth.getInstance()
                firebaseAuth.signOut()

                val profileIntent = Intent(this, Login::class.java)
                val options = ActivityOptions.makeSceneTransitionAnimation(this)
                startActivity(profileIntent, options.toBundle())
            }

            val dialog = builder.create()
            dialog.show()
        }

        fetchBoughtProducts()
    }

    private fun fetchBoughtProducts() {
        val database = FirebaseDatabase.getInstance()
        val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid

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
                Toast.makeText(applicationContext, "Error fetching bought products", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
