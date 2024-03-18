package com.assignment.findit

import SellUploadClass
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class Sell : AppCompatActivity() {

    lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sell)

        // Get Firebase Authentication instance
        mAuth = FirebaseAuth.getInstance()

        // **Using synthetic binding (recommended):**
        // All views from activity_sell.xml are automatically accessible

        // **Alternatively, using findViewById (optional):**

        val sellerNameEdt = findViewById<EditText>(R.id.sellerNameEdt)
        val productNameEdt = findViewById<EditText>(R.id.productNameEdt)
        val locationEdt = findViewById<EditText>(R.id.locationEdt)
        val priceEdt = findViewById<EditText>(R.id.priceEdt)
        val phonenoEdt = findViewById<EditText>(R.id.phonenoEdt)
        val sellBtn = findViewById<Button>(R.id.sellBtn)


        // Set up button click listener
        sellBtn.setOnClickListener {
            val sellerName = sellerNameEdt.text.toString().trim()
            val productName = productNameEdt.text.toString().trim()
            val location = locationEdt.text.toString().trim()
            val phno = phonenoEdt.text.toString().trim()
            var price: Int

            // Input validation (optional: you can further customize error messages)
            if (TextUtils.isEmpty(sellerName)) {
                sellerNameEdt.requestFocus()
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(productName)) {
                productNameEdt.requestFocus()
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(location)) {
                locationEdt.requestFocus()
                return@setOnClickListener
            }
            if (TextUtils.isEmpty(phno)) {
                phonenoEdt.requestFocus()
                return@setOnClickListener
            }

            try {
                price = Integer.parseInt(priceEdt.text.toString().trim())
                // Clear any previous error text
            } catch (e: NumberFormatException) {
                price = 0
                priceEdt.requestFocus()
                return@setOnClickListener
            }

            // **Data retrieval and writing:**
            val currentUserId = getCurrentUserId()

            val sellUploadClass = SellUploadClass(
                sellerName,
                productName,
                location,
                price,
                currentUserId,
                "no",
                "",
                phno
            )

            writeToDatabaseForGlobal(sellUploadClass)
            writeToDatabase(sellUploadClass)

            // Clear input fields after successful upload (optional)
            sellerNameEdt.setText("")
            productNameEdt.setText("")
            locationEdt.setText("")
            priceEdt.setText("")
            phonenoEdt.setText("")
        }
    }

    private fun writeToDatabaseForGlobal(sellUploadClass: SellUploadClass) {
        val database = FirebaseDatabase.getInstance()
        val rootRef = database.reference.child("FindIt").child("allSold").push() // Use push() for unique key

        rootRef.setValue(sellUploadClass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Toast message can be removed if you prefer
                    Toast.makeText(this, "Data Uploaded to allSold", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun writeToDatabase(sellUploadClass: SellUploadClass) {
        val database = FirebaseDatabase.getInstance()
        val rootRef = database.reference.child("FindIt").child("users")
        val userRef = rootRef.child(sellUploadClass.sellerId).child("sell") // Use sellerId

        userRef.push().setValue(sellUploadClass) // Use push() for unique key
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Data Uploaded", Toast.LENGTH_SHORT).show()
                    // (Optional) Clear fields or perform other actions after successful upload
                } else {
                    Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun getCurrentUserId(): String {
        val currentUser = FirebaseAuth.getInstance().currentUser
        return if (currentUser != null) {
            currentUser.uid!! // Use !! for non-null assertion after null check
        } else {
            return ""
        }
        }
    }