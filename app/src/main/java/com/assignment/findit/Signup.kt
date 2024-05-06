package com.assignment.findit

import android.app.ActivityOptions
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.transition.Slide
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class Signup : AppCompatActivity() {
    lateinit var auth: FirebaseAuth
    lateinit var emailEdt: EditText
    lateinit var passEdt: EditText
    lateinit var confPassEdt: EditText
    lateinit var nameEdt: EditText  // New EditText for name
    lateinit var phoneEdt: EditText  // New EditText for phone number
    lateinit var addressEdt: EditText // New EditText for address
    lateinit var signupBtn: AppCompatButton
    lateinit var logInBtn: AppCompatButton
    lateinit var database: FirebaseDatabase  // Added for database access
    lateinit var userRef: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.enterTransition = Slide(Gravity.END)
        window.exitTransition = Slide(Gravity.START)
        setContentView(R.layout.activity_signup)
        auth = FirebaseAuth.getInstance()
        emailEdt = findViewById(R.id.editTextText4)
        passEdt = findViewById(R.id.editTextText5)
        confPassEdt = findViewById(R.id.editTextText6)
        nameEdt = findViewById(R.id.nameSignUpEdt)  // Reference new EditText
        phoneEdt = findViewById(R.id.phnoSignUpEdt)  // Reference new EditText
        addressEdt = findViewById(R.id.addressSignUpEdt) // Reference new EditText
        signupBtn = findViewById(R.id.button3)
        logInBtn = findViewById(R.id.button4)
        database = FirebaseDatabase.getInstance()

        signupBtn.setOnClickListener {
            val email = emailEdt.text.toString()
            val password = passEdt.text.toString()
            val confirmPassword = confPassEdt.text.toString()
            val name = nameEdt.text.toString().trim()  // Get name with trim()
            val phone = phoneEdt.text.toString().trim() // Get phone number with trim()
            val address = addressEdt.text.toString().trim()  // Get address with trim()

            // Check for empty fields
            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword) || TextUtils.isEmpty(name) || TextUtils.isEmpty(phone) || TextUtils.isEmpty(address)) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else {
                // All fields are filled, proceed with signup
                if (password == confirmPassword) {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnSuccessListener {
                            val userId = auth.currentUser?.uid  // Get current user ID

                            // Create a User data object (optional, modify if needed)
                            val userData = hashMapOf(
                                "name" to name,
                                "phone" to phone,
                                "address" to address
                            )

                            // Write user data to database under the specified path
                            if (userId != null) {
                                val userPath = "FindIt/UserDetails/$userId"  // Modified path
                                database.getReference(userPath).setValue(userData)
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show()
                                        val profileIntent = Intent(this, Login::class.java)
                                        val options = ActivityOptions.makeSceneTransitionAnimation(this)
                                        startActivity(profileIntent, options.toBundle())
                                        Toast.makeText(this, "Log In", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener { exception ->
                                        Toast.makeText(this, "Failed to add user data: $exception", Toast.LENGTH_SHORT).show()
                                    }
                            } else {
                                Toast.makeText(this, "Failed to create account", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(this, "Signup failed: $exception", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                }
            }
        }



        logInBtn = findViewById(R.id.button4)
        logInBtn.setOnClickListener {
            finishAfterTransition()
        }

    }
}