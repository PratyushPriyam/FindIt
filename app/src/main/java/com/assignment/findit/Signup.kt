package com.assignment.findit

import android.app.ActivityOptions
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.transition.Slide
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import com.google.firebase.auth.FirebaseAuth

class Signup : AppCompatActivity() {
    lateinit var auth: FirebaseAuth
    lateinit var emailEdt: EditText
    lateinit var passEdt: EditText
    lateinit var confPassEdt: EditText
    lateinit var signupBtn: AppCompatButton
    lateinit var logInBtn: AppCompatButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.enterTransition = Slide(Gravity.LEFT)
        setContentView(R.layout.activity_signup)
        auth = FirebaseAuth.getInstance()
        emailEdt = findViewById(R.id.editTextText4)
        passEdt = findViewById(R.id.editTextText5)
        confPassEdt = findViewById(R.id.editTextText6)
        signupBtn = findViewById(R.id.button3)

        signupBtn.setOnClickListener {
            auth.createUserWithEmailAndPassword(emailEdt.text.toString(), passEdt.text.toString()).addOnSuccessListener {
                Toast.makeText(this, "Now log in", Toast.LENGTH_SHORT).show()
                val profileIntent = Intent(this, Login::class.java)
                val options = ActivityOptions.makeSceneTransitionAnimation(this)
                startActivity(profileIntent, options.toBundle())
            }.addOnFailureListener {
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
            }
        }

        logInBtn = findViewById(R.id.button4)
        logInBtn.setOnClickListener {
            finishAfterTransition()
        }

    }
}