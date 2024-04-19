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
import android.provider.Settings
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream

class Sell : AppCompatActivity() {

    lateinit var mAuth: FirebaseAuth
    lateinit var uidEdt: EditText
    private lateinit var imageView: ImageView
    private val REQUEST_IMAGE_CAPTURE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sell)

        // Get Firebase Authentication instance
        mAuth = FirebaseAuth.getInstance()

        val sellerNameEdt = findViewById<EditText>(R.id.sellerNameEdt)
        val productNameEdt = findViewById<EditText>(R.id.productNameEdt)
        val locationEdt = findViewById<EditText>(R.id.locationEdt)
        val priceEdt = findViewById<EditText>(R.id.priceEdt)
        val phonenoEdt = findViewById<EditText>(R.id.phonenoEdt)
        uidEdt = findViewById(R.id.uidedt)
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
            val sellerName = sellerNameEdt.text.toString().trim()
            val productName = productNameEdt.text.toString().trim()
            val location = locationEdt.text.toString().trim()
            var phno: String
            val uniqueid = uidEdt.text.toString().trim()
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
            if (TextUtils.isEmpty(phonenoEdt.text.toString().trim())) {
                phno = "" // Handle empty phone number (optional)
            } else {
                phno = phonenoEdt.text.toString().trim()
            }
            if (TextUtils.isEmpty(uniqueid)) {
                uidEdt.requestFocus()
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

            // Check for existing ID before adding
            checkForExistingId(uniqueid, currentUserId) { isUnique ->
                if (isUnique) {
                    val capturedImage = imageView.drawable
                    if (capturedImage != null) {
                        val bitmap = (capturedImage as BitmapDrawable).bitmap

                        uploadImageToFirebase(bitmap) { imageUrl ->
                            val sellUploadClass = SellUploadClass(
                                sellerName,
                                productName,
                                location,
                                price,
                                currentUserId,
                                "no",
                                "",
                                phno,
                                uniqueid,
                                false,
                                imageUrl
                            )
                            writeToDatabaseForGlobal(sellUploadClass)
                            writeToDatabase(sellUploadClass)

                            // Clear input fields after successful upload (optional)
                            sellerNameEdt.setText("")
                            productNameEdt.setText("")
                            locationEdt.setText("")
                            priceEdt.setText("")
                            phonenoEdt.setText("")
                            uidEdt.setText("")
                            imageView.setImageDrawable(null) // Clear image view after upload
                        }
                    } else {
                        // No image captured, proceed without image
                        val sellUploadClass = SellUploadClass(
                            sellerName,
                            productName,
                            location,
                            price,
                            currentUserId,
                            "no",
                            "",
                            phno,
                            uniqueid,
                            false,
                            ""// No image URL if not captured
                        )
                        writeToDatabaseForGlobal(sellUploadClass)
                        writeToDatabase(sellUploadClass)

                        // Clear input fields after successful upload (optional)
                        sellerNameEdt.setText("")
                        productNameEdt.setText("")
                        locationEdt.setText("")
                        priceEdt.setText("")
                        phonenoEdt.setText("")
                        uidEdt.setText("")
                    }
                } else {
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
        val rootRef = database.reference.child("FindIt").child("allSold").child(uidEdt.text.toString()) // Use push() for unique key

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

    private fun uploadImageToFirebase(bitmap: Bitmap, callback: (String) -> Unit) {
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("images/${System.currentTimeMillis()}.jpg") // Create unique image name

        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos) // Compress image for efficient storage
        val imageData = baos.toByteArray()

        val uploadTask = imageRef.putBytes(imageData)
        uploadTask.addOnSuccessListener {
            it.storage.downloadUrl.addOnSuccessListener { uri ->
                val imageUrl = uri.toString()
                callback(imageUrl) // Call the provided callback function with the download URL
            }
        }.addOnFailureListener { exception ->
            // Handle image upload failure
            Toast.makeText(this, "Image upload failed: ${exception.message}", Toast.LENGTH_SHORT).show()
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