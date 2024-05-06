package com.assignment.findit

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.format.Formatter
import android.transition.Slide
import android.view.Gravity
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Profile : AppCompatActivity() {

    private lateinit var userName: EditText
    private lateinit var userAddress: EditText
    private lateinit var userPhNo: EditText
    private lateinit var saveButton: androidx.appcompat.widget.AppCompatButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.enterTransition = Slide(Gravity.END)
        window.exitTransition = Slide(Gravity.START)
        setContentView(R.layout.activity_profile)

        userName = findViewById(R.id.userName)
        userAddress = findViewById(R.id.userAddress)
        userPhNo = findViewById(R.id.userPhNo)
        saveButton = findViewById(R.id.saveProfileButton)

        val backBtn = findViewById<AppCompatButton>(R.id.backBtn)
        backBtn.setOnClickListener {
            finishAfterTransition()
        }

        // Fetch user details from Firebase
        fetchUserDetails()

        // Get WIFI IP Address
        enableWifi()

        saveButton.setOnClickListener {
            showUpdateConfirmationDialog()
        }
    }

    private fun fetchUserDetails() {
        val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
        val database = FirebaseDatabase.getInstance()
        val reference = database.getReference("FindIt/UserDetails/$currentUserId")

        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val user = snapshot.getValue(UserDetails::class.java)!!
                    userName.setText(user.name)
                    userAddress.setText(user.address)
                    userPhNo.setText(user.phone)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database errors here
                Toast.makeText(this@Profile, "Error fetching data!", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showUpdateConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirm Profile Update")
        val updatedName = userName.text.toString().trim()
        val updatedAddress = userAddress.text.toString().trim()
        val updatedPhone = userPhNo.text.toString().trim()
        builder.setMessage("Are you sure you want to update your profile with the following details?\n\nName: $updatedName\nAddress: $updatedAddress\nPhone: $updatedPhone")
        builder.setPositiveButton("Update") { dialog, which ->
            updateUserDetails(updatedName, updatedAddress, updatedPhone)
        }
        builder.setNegativeButton("Cancel") { dialog, which ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun updateUserDetails(name: String, address: String, phone: String) {
        if (name.isEmpty() || address.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields!", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
        val database = FirebaseDatabase.getInstance()
        val reference = database.getReference("FindIt/UserDetails/$currentUserId")

        val user = UserDetails(name, address, phone)

        reference.setValue(user)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    val error = task.exception
                    Toast.makeText(this, "Error updating profile: $error", Toast.LENGTH_SHORT).show()
                }
            }
    }
    private fun enableWifi() {
        val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val winfo = wifiManager.connectionInfo
        val ipaddress = Formatter.formatIpAddress(winfo.ipAddress)
        val iptv = findViewById<TextView>(R.id.tvip)
        iptv.text = "Your current ip address is: " + ipaddress
    }
}

// Data class for UserDetails (modify if needed)
data class UserDetails(val name: String, val address: String, val phone: String) {
    // Add an empty constructor
    constructor() : this("", "", "")
}
