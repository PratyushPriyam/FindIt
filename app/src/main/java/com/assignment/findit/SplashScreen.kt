package com.assignment.findit

import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.transition.Slide
import android.view.Gravity
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SplashScreen : AppCompatActivity(), SensorEventListener {

    lateinit var imageView: ImageView
    lateinit var progressBar: ProgressBar
    lateinit var brightnesstv: TextView
    private lateinit var sensorManager: SensorManager
    private var lightSensor: Sensor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.enterTransition = Slide(Gravity.END)
        window.exitTransition = Slide(Gravity.START)
        setContentView(R.layout.activity_splash_screen)

        progressBar = findViewById(R.id.progressBar2)
        imageView = findViewById(R.id.imageView)
        imageView.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.blink))

        // Light Sensor
        brightnesstv = findViewById(R.id.brightnesstv)
        setUpLightSensor()

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

    private fun setUpLightSensor() {
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
            val lightValue = event.values[0]
            showToastBasedOnLight(lightValue)
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {  }
    private fun showToastBasedOnLight(lightValue: Float) {
        val brightnessLevel = getBrightnessLevel(lightValue)
        val message = when (brightnessLevel) {
            "Bright" -> "It's too bright outside! Increase phone brightness for better viewing."
            "Dark" -> "It's dark outside! Reduce phone brightness to avoid eye strain."
            else -> "Just the right amount of light"
        }
        if (message.isNotEmpty()) {
            brightnesstv.text = message
        }
    }
    private fun getBrightnessLevel(lightValue: Float): String {
        return when (lightValue.toInt()) {
            0 -> "Dark"
            in 1..10 -> "Dark"
            in 11..50 -> "Normal"
            in 51..5000 -> "Normal"
            in 5001..25000 -> "Bright"
            else -> "Bright"
        }
    }
    override fun onResume() {
        super.onResume()
        lightSensor?.also { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
    }
}
