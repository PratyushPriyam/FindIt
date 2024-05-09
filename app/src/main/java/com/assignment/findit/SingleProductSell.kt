package com.assignment.findit

import SellUploadClass
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.transition.Slide
import android.view.Gravity
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Locale

class SingleProductSell : AppCompatActivity() {
    lateinit var imgView: ImageView
    lateinit var soldby: TextView
    lateinit var location: TextView
    lateinit var phno: TextView
    lateinit var sellerid: TextView
    lateinit var qty_tv: TextView
    lateinit var buybtn: AppCompatButton
    lateinit var database: FirebaseDatabase
    lateinit var productRef: DatabaseReference
    lateinit var productList: ArrayList<HashMap<String, Any>>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.enterTransition = Slide(Gravity.END)
        window.exitTransition = Slide(Gravity.START)
        setContentView(R.layout.activity_single_product_sell)

        imgView = findViewById(R.id.imageView3)
        soldby = findViewById(R.id.soldbytv)
        qty_tv = findViewById(R.id.qty_tv)
        phno = findViewById(R.id.phnotv)
        location = findViewById(R.id.loctv)
        sellerid = findViewById(R.id.idtv)
        buybtn = findViewById(R.id.buybtn)
        productList = ArrayList<HashMap<String, Any>>()

        // Retrieve product details from intent
        val productImgUrl = intent.getStringExtra("productimg")
        val soldBy = intent.getStringExtra("soldby")
        val ilocation = intent.getStringExtra("location")
        val sellerId = intent.getStringExtra("sellerid")
        val iphno = intent.getStringExtra("phno")
        val uid = intent.getStringExtra("uid")
        val qty = intent.getIntExtra("qty", 0)

        // Set product details to the views
        Glide.with(this).load(productImgUrl).into(imgView) // Use Glide for image loading
        soldby.text = "Sold By: $soldBy"
        phno.text = "Phone Number: $iphno"
        location.text = "Location: $ilocation"
        sellerid.text = "Seller ID: $sellerId"
        qty_tv.text = "Qty: $qty"

        val backBtn = findViewById<AppCompatButton>(R.id.backBtn)
        backBtn.setOnClickListener {
            finishAfterTransition()
        }

        // Google map integration
        val mapLoc = findViewById<ImageView>(R.id.mapLoc)
        mapLoc.setOnClickListener {
            val sellerLocation = location.text.toString().trim() // Extract location text
            if (sellerLocation.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_VIEW,
                    Uri.parse("geo:0,0?q=$sellerLocation")) // Intent to open Google Maps app
                intent.setPackage("com.google.android.apps.maps") // Specify Google Maps app package
                startActivity(intent)
            } else {
                Toast.makeText(this, "Seller location not available", Toast.LENGTH_SHORT).show()
            }
        }


        buybtn.setOnClickListener {
            database = FirebaseDatabase.getInstance()
            val reference = database.getReference("FindIt/allSold")

            database = FirebaseDatabase.getInstance()
            buybtn.text = "Press again to sent buying request"

            buybtn.setOnClickListener {
                val currentUserId = getCurrentUserId()

                // Construct a reference to the specific product using uid
                val productRef = database.getReference("FindIt/allSold").child(uid!!)

                productRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val product = snapshot.getValue(SellUploadClass::class.java)!!

                            // Check if sellerId matches and product isn't already bought or pending
                            if (product.sellerId == sellerId && product.isBought == "no") {
                                // Set isBought to "pending" and buyer ID
                                product.isBought = "pending"
                                product.boughtBy = currentUserId

                                // Update the product data in Firebase
                                snapshot.ref.setValue(product)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            Toast.makeText(
                                                applicationContext,
                                                "Purchase request sent!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            startActivity(Intent(applicationContext, MainActivity::class.java))
                                        } else {
                                            Toast.makeText(
                                                applicationContext,
                                                "Failed to request purchase",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            // Handle update failure (e.g., log the error)
                                        }
                                    }
                            } else {
                                // Handle scenario where product is not found, already bought, or pending
                                Toast.makeText(
                                    applicationContext,
                                    "Product not found, already bought, or pending",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            // Handle scenario where the product with the given uid doesn't exist
                            Toast.makeText(
                                applicationContext,
                                "Product not found",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle database errors here
                    }
                })
            }

        }
    }

    private fun getCurrentUserId(): String {
        val currentUser = FirebaseAuth.getInstance().currentUser
        return if (currentUser != null) {
            currentUser.uid
        } else {
            // Handle the case where there's no logged-in user
            ""
        }
    }

}
