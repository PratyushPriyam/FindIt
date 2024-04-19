package com.assignment.findit

import SellUploadClass
import android.app.ActivityOptions
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import com.google.android.gms.tasks.OnCompleteListener
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.widget.ContentLoadingProgressBar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var productList: ArrayList<SellUploadClass>
    lateinit var adapter: RecyclerView.Adapter<*>
    private lateinit var btn: Button
    private lateinit var btnProcess: Button
    private lateinit var btnAdd: Button
    private lateinit var layoutSwitch: SwitchCompat
    private lateinit var planeLoad: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recview)
        productList = ArrayList()
        adapter = ProductAdapter(this, productList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        planeLoad = findViewById(R.id.planeLoad)

        layoutSwitch = findViewById(R.id.switchbtn)

        layoutSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Switch to grid layout
                adapter = ProductAdapterGrid(this, productList)
                recyclerView.layoutManager = GridLayoutManager(this, 2)
            } else {
                // Switch to linear layout
                adapter = ProductAdapter(this, productList)
                recyclerView.layoutManager = LinearLayoutManager(this)
            }
            recyclerView.adapter = adapter
        }

        val subButtonAnimationHorizontal = AnimationUtils.loadAnimation(this, R.anim.sub_button_animation_horizontal)
        val subButtonAnimationVertical = AnimationUtils.loadAnimation(this, R.anim.sub_button_animation_vertical)
        val subButtonAnimationDiagonal = AnimationUtils.loadAnimation(this, R.anim.sub_button_animation_diagonal)
        val logOutEntry = AnimationUtils.loadAnimation(this, R.anim.sub_button_logout_entry)


        val floatingActionButton = findViewById<LottieAnimationView>(R.id.floatingActionButton)
        val fabSubBtn1 = findViewById<FloatingActionButton>(R.id.personFab)
        val fabSubBtn2 = findViewById<FloatingActionButton>(R.id.pendingFab)
        val fabSubBtn3 = findViewById<FloatingActionButton>(R.id.addtoinventoryFab)
        val logOutFab = findViewById<FloatingActionButton>(R.id.logoutFab)
        var isRotated = false
        floatingActionButton.setOnClickListener {
            val anim = RotateAnimation(
                0f,
                45f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
            )
            anim.duration = 200
            anim.fillAfter = true
            anim.interpolator = LinearInterpolator()

            val reverseAnim = RotateAnimation(
                70f, // Start from 45 degrees (reversed)
                0f, // Rotate to 0 degrees
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
            )
            reverseAnim.duration = 200
            reverseAnim.fillAfter = true
            reverseAnim.interpolator = LinearInterpolator()

            fabSubBtn1.setOnClickListener {
                val profileIntent = Intent(this, Buy::class.java)
                val options = ActivityOptions.makeSceneTransitionAnimation(this)
                startActivity(profileIntent, options.toBundle())
            }

            fabSubBtn3.setOnClickListener {
                val historyIntent = Intent(this, Sell::class.java)
                val options = ActivityOptions.makeSceneTransitionAnimation(this)
                startActivity(historyIntent, options.toBundle())
            }

            fabSubBtn2.setOnClickListener {
                val exploreIntent = Intent(this, ProcessingSell::class.java)
                val options = ActivityOptions.makeSceneTransitionAnimation(this)
                startActivity(exploreIntent, options.toBundle())
            }
            logOutFab.setOnClickListener {
                // Inflate the custom layout for the dialog
                val view = LayoutInflater.from(this).inflate(R.layout.custom_layout_dialog, null)

                // Create an AlertDialog builder
                val builder = AlertDialog.Builder(this)

                // Set the custom view for the dialog
                builder.setView(view)

                // Find the buttons from the custom layout
                val yesButton = view.findViewById<Button>(R.id.yesButton)

                // Set positive (Yes) button click listener
                yesButton.setOnClickListener {
                    // User clicked "Yes", proceed with logout
                    val firebaseAuth = FirebaseAuth.getInstance()
                    firebaseAuth.signOut()

                    val profileIntent = Intent(this, Login::class.java)
                    val options = ActivityOptions.makeSceneTransitionAnimation(this)
                    startActivity(profileIntent, options.toBundle())
                }

                // Create and show the alert dialog
                val dialog = builder.create()
                dialog.show()
            }


            if (isRotated) {
                floatingActionButton.startAnimation(reverseAnim) // Use reverseAnim for reversed rotation
                isRotated = false
                isRotated = false
                fabSubBtn1.visibility = View.GONE
                fabSubBtn2.visibility = View.GONE
                fabSubBtn3.visibility = View.GONE
                logOutFab.visibility = View.GONE

                fabSubBtn1.startAnimation(
                    AnimationUtils.loadAnimation(
                        this,
                        R.anim.sub_button_animation_vertical_back
                    )
                )
                fabSubBtn2.startAnimation(
                    AnimationUtils.loadAnimation(
                        this,
                        R.anim.sub_button_animation_diagonal_back
                    )
                )
                fabSubBtn3.startAnimation(
                    AnimationUtils.loadAnimation(
                        this,
                        R.anim.sub_button_animation_horizantal_back
                    )
                )
                logOutFab.startAnimation(
                    AnimationUtils.loadAnimation(
                        this,
                        R.anim.sub_button_logout_exit
                    )
                )
            } else {
                floatingActionButton.startAnimation(anim)
                fabSubBtn1.visibility = View.VISIBLE
                fabSubBtn2.visibility = View.VISIBLE
                fabSubBtn3.visibility = View.VISIBLE
                logOutFab.visibility = View.VISIBLE
                fabSubBtn1.startAnimation(subButtonAnimationVertical)
                fabSubBtn2.startAnimation(subButtonAnimationDiagonal)
                fabSubBtn3.startAnimation(subButtonAnimationHorizontal)
                logOutFab.startAnimation(logOutEntry)
                isRotated = true
            }
        }

        // Fetch products from Firebase
        fetchProducts()
    }

    private fun fetchProducts() {
        val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
        val database = FirebaseDatabase.getInstance()
        val reference = database.getReference("FindIt/allSold")

        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                productList.clear()
                for (dataSnapshot in snapshot.children) {
                    val product = dataSnapshot.getValue(SellUploadClass::class.java)!!
                    if (product.isBought == "no" && product.sellerId != currentUserId) {
                        productList.add(product)
                    }
                }

                // Hide loading animation after data is fetched
                planeLoad.visibility = View.GONE

                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database errors here
                planeLoad.visibility = View.GONE  // Hide loading animation on error
            }
        })
    }
}
