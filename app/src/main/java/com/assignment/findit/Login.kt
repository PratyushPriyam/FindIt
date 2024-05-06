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

class Login : AppCompatActivity() {
    lateinit var emailEdt: EditText
    lateinit var passEdt: EditText
    lateinit var logInBtn: AppCompatButton
    lateinit var signUpBtn: AppCompatButton
    lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.enterTransition = Slide(Gravity.END)
        window.exitTransition = Slide(Gravity.START)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        emailEdt = findViewById(R.id.editTextText)
        passEdt = findViewById(R.id.editTextText3)
        signUpBtn = findViewById(R.id.button2)
        signUpBtn.setOnClickListener {
            val profileIntent = Intent(this, Signup::class.java)
            val options = ActivityOptions.makeSceneTransitionAnimation(this)
            startActivity(profileIntent, options.toBundle())
        }

        logInBtn = findViewById(R.id.button)
        logInBtn.setOnClickListener {
            auth.signInWithEmailAndPassword(emailEdt.text.toString(), passEdt.text.toString()).addOnSuccessListener {
                Toast.makeText(this, "Logged in", Toast.LENGTH_SHORT).show()
                val profileIntent = Intent(this, MainActivity::class.java)
                val options = ActivityOptions.makeSceneTransitionAnimation(this)
                startActivity(profileIntent, options.toBundle())
            }
                .addOnFailureListener {
                    Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
                }
        }
    }
}