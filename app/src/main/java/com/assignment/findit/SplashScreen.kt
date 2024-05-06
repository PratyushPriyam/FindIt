package com.assignment.findit

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.transition.Slide
import android.view.Gravity
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SplashScreen : AppCompatActivity() {

    lateinit var imageView: ImageView
    lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.enterTransition = Slide(Gravity.END)
        window.exitTransition = Slide(Gravity.START)
        setContentView(R.layout.activity_splash_screen)

        progressBar = findViewById(R.id.progressBar2)
        imageView = findViewById(R.id.imageView)
        imageView.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.blink))

        // Simulate progress bar filling in 3 seconds
        val handler = Handler()
        val runnable: Runnable = object : Runnable {
            var progress = 0

            override fun run() {
                if (progress < 100) {
                    progress += 10
                    progressBar.progress = progress
                    handler.postDelayed(this, 500) // Update every 300 milliseconds
                } else {
                    val firebaseAuth = FirebaseAuth.getInstance()
                    val currentUser = firebaseAuth.currentUser

                    if (currentUser == null) {
                        startActivity(Intent(this@SplashScreen, Login::class.java))
                    } else {
                        startActivity(Intent(this@SplashScreen, MainActivity::class.java))
                    }
                    finish() // Finish the splash screen after navigation
                }
            }
        }
        handler.post(runnable)
    }
}
