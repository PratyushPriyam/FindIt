package com.assignment.findit

import SellUploadClass
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import android.Manifest
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.service.autofill.UserData
import android.view.View
import android.widget.ProgressBar
import androidx.annotation.RequiresApi
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import kotlin.random.Random

class Sell : AppCompatActivity() {

    lateinit var mAuth: FirebaseAuth
    lateinit var uidEdt: String
    private lateinit var imageView: ImageView
    private val REQUEST_IMAGE_CAPTURE = 101
    lateinit var database: FirebaseDatabase
    lateinit var userRef: DatabaseReference
    lateinit var auth: FirebaseAuth
    lateinit var sellerName: String
    lateinit var phno: String
    lateinit var location: String
    lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sell)

        // Get Firebase Authentication instance
        mAuth = FirebaseAuth.getInstance()
        uidEdt = Random.nextInt(1, 1001).toString().trim()

        val productNameEdt = findViewById<EditText>(R.id.productNameEdt)
        progressBar = findViewById(R.id.progressBar)
        val sellBtn = findViewById<Button>(R.id.sellBtn)
        imageView = findViewById(R.id.imgView)

        // Fab clicking - image capture
        val fab = findViewById<FloatingActionButton>(R.id.camera_fab) // Assuming ID for FAB
        fab.setOnClickListener {
            if (checkCameraPermission()) {
                // Open camera intent
                val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                if (takePictureIntent.resolveActivity(packageManager) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            } else {
                // Explain permission and guide to settings (can't directly open permission screen)
                Toast.makeText(this, "Camera permission is required to capture an image. Please grant permission in your app settings.", Toast.LENGTH_LONG).show()

                // Optionally, open the system settings app (might not lead to direct permission screen)
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            }
        }

        val backBtn = findViewById<AppCompatButton>(R.id.backBtn)
        backBtn.setOnClickListener {
            finishAfterTransition()
        }

        // Set up button click listener
        sellBtn.setOnClickListener {
            val productName = productNameEdt.text.toString().trim()
            val productQtyEdt = findViewById<EditText>(R.id.productQtyEdt)
            val productQty = productQtyEdt.text.toString().trim().toIntOrNull() ?: 0


            if (TextUtils.isEmpty(productName)) {
                productNameEdt.requestFocus()
                return@setOnClickListener
            }

            // Make progress bar visible
            progressBar.visibility = View.VISIBLE

            // **Data retrieval and writing:**
            val currentUserId = getCurrentUserId()
            database = FirebaseDatabase.getInstance()
            auth = FirebaseAuth.getInstance()
            val userId = auth.currentUser?.uid

            if (userId != null) {
                userRef = database.getReference("FindIt/UserDetails/$userId")

                userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    @RequiresApi(Build.VERSION_CODES.P)
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val userData = snapshot.value as? Map<*, *>
                            userData?.let {
                                val name = it["name"] as? String
                                val address = it["address"] as? String
                                val phone = it["phone"] as? String
                                Toast.makeText(applicationContext, "$name, $address, $phone", Toast.LENGTH_SHORT).show()
                                location = address.toString()
                                sellerName = name.toString()
                                phno = phone.toString()
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle database error
                    }
                })
            }

            // Check for existing ID before adding
            checkForExistingId(uidEdt, currentUserId) { isUnique ->
                if (isUnique) {
                    val capturedImage = imageView.drawable
                    if (capturedImage != null) {
                        val bitmap = (capturedImage as BitmapDrawable).bitmap

                        uploadImageToFirebase(bitmap) { imageUrl ->
                            val sellUploadClass = SellUploadClass(
                                sellerName,
                                productName,
                                location,
                                currentUserId,
                                "no",
                                "",
                                phno,
                                uidEdt,
                                false,
                                imageUrl,
                                productQty
                            )
                            writeToDatabaseForGlobal(sellUploadClass)
                            writeToDatabase(sellUploadClass)

                            productNameEdt.setText("")
                            imageView.setImageDrawable(null) // Clear image view after upload
                        }
                    } else {
                        // No image captured, proceed without image
                        val sellUploadClass = SellUploadClass(
                            sellerName,
                            productName,
                            location,
                            currentUserId,
                            "no",
                            "",
                            phno,
                            uidEdt,
                            false,
                            "",
                            productQty
                        )
                        writeToDatabaseForGlobal(sellUploadClass)
                        writeToDatabase(sellUploadClass)

                        progressBar.visibility = View.GONE
                        productNameEdt.setText("")
                    }
                } else {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this, "This unique ID is already used!", Toast.LENGTH_SHORT).show()
                }
            }

        }

    }

    fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            REQUEST_IMAGE_CAPTURE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            val bitmap = data?.extras?.get("data") as Bitmap
            imageView.setImageBitmap(bitmap)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with camera intent
                Toast.makeText(this, "Camera permission granted!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Camera permission required to capture image!", Toast.LENGTH_SHORT).show()
                // You may also want to explain to the user why your app needs camera access and guide them to settings if they choose to deny permission.
            }
        }
    }

    private fun writeToDatabaseForGlobal(sellUploadClass: SellUploadClass) {
        val database = FirebaseDatabase.getInstance()
        val rootRef = database.reference.child("FindIt").child("allSold").child(uidEdt.toString()) // Use push() for unique key

        rootRef.setValue(sellUploadClass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Toast message can be removed if you prefer
                    Toast.makeText(this, "Data Uploaded to allSold", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                } else {
                    Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun uploadImageToFirebase(bitmap: Bitmap, callback: (String) -> Unit) {
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("images/${System.currentTimeMillis()}.jpg") // Create unique image name

        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageData = baos.toByteArray()

        val uploadTask = imageRef.putBytes(imageData)
        uploadTask.addOnSuccessListener {
            it.storage.downloadUrl.addOnSuccessListener { uri ->
                val imageUrl = uri.toString()
                callback(imageUrl)
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(this, "Image upload failed: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }


    private fun writeToDatabase(sellUploadClass: SellUploadClass) {
        val database = FirebaseDatabase.getInstance()
        val rootRef = database.reference.child("FindIt").child("users")
        val userRef = rootRef.child(sellUploadClass.sellerId).child("sell")

        userRef.push().setValue(sellUploadClass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Data Uploaded", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun getCurrentUserId(): String {
        val currentUser = FirebaseAuth.getInstance().currentUser
        return if (currentUser != null) {
            currentUser.uid!!
        } else {
            return ""
        }
    }
    private fun checkForExistingId(uniqueId: String, currentUserId: String, callback: (Boolean) -> Unit) {
        val database = FirebaseDatabase.getInstance()
        val userRef = database.reference.child("FindIt").child("users").child(currentUserId).child("sell")

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var isUnique = true
                for (child in dataSnapshot.children) {
                    val existingId = child.child("uid").getValue(String::class.java) ?: ""
                    if (existingId == uniqueId) {
                        isUnique = false
                        break
                    }
                }
                callback(isUnique)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database errors (optional)
                Toast.makeText(this@Sell, "Error checking ID", Toast.LENGTH_SHORT).show()
            }
        })
    }
}