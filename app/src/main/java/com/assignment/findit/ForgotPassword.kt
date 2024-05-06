package com.assignment.findit

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.transition.Slide
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.google.firebase.auth.FirebaseAuth

class ForgotPassword : AppCompatActivity() {

    private lateinit var forgotEmail: EditText
    private lateinit var forgotBtn: AppCompatButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.enterTransition = Slide(Gravity.END)
        window.exitTransition = Slide(Gravity.START)
        setContentView(R.layout.activity_forgot_password)

        forgotEmail = findViewById(R.id.forgotEdt)
        forgotBtn = findViewById(R.id.forgotBtn)

        val backBtn = findViewById<AppCompatButton>(R.id.backBtn)
        backBtn.setOnClickListener {
            finishAfterTransition()
        }

        forgotBtn.setOnClickListener {
            sendPasswordResetEmail()
        }
    }

    private fun sendPasswordResetEmail() {
        val email = forgotEmail.text.toString().trim()

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter your email address!", Toast.LENGTH_SHORT).show()
            return
        }

        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Password reset email sent successfully!", Toast.LENGTH_SHORT).show()
                    forgotEmail.text.clear() // Optionally clear the email field
                } else {
                    val error = task.exception
                    Toast.makeText(this, "Error sending password reset email: $error", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
