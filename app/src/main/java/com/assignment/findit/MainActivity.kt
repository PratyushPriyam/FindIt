package com.assignment.findit

import SellUploadClass
import android.app.ActivityOptions
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.transition.Slide
import com.google.android.gms.tasks.OnCompleteListener
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
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
    private lateinit var donationInfoTxt: TextView
    private lateinit var filterEdt: EditText
    private lateinit var filterImg: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.enterTransition = Slide(Gravity.END)
        window.exitTransition = Slide(Gravity.START)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recview)
        productList = ArrayList()
        adapter = ProductAdapter(this, productList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        planeLoad = findViewById(R.id.planeLoad)
        donationInfoTxt = findViewById(R.id.donationInfoTxt)

        layoutSwitch = findViewById(R.id.switchbtn)

        // Searching Option
        filterEdt = findViewById(R.id.searchEdt) // Replace with your EditText id
        filterImg = findViewById(R.id.searchImg) // Replace with your ImageView id

        filterImg.setOnClickListener {
            val filterText = filterEdt.text.toString().trim()
            if (filterText.isNotEmpty()) {
                val filteredList = productList.filter { product ->
                    product.productName.toLowerCase().contains(filterText.toLowerCase())
                } as ArrayList<SellUploadClass>
                adapter = ProductAdapter(this, filteredList)
                recyclerView.adapter = adapter
                adapter.notifyDataSetChanged()
            } else {
                // Reset filter to full product list
                adapter = ProductAdapter(this, productList)
                recyclerView.adapter = adapter
                adapter.notifyDataSetChanged()
            }
        }

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


        val floatingActionButton = findViewById<LottieAnimationView>(R.id.floatingActionButton)
        val fabSubBtn1 = findViewById<FloatingActionButton>(R.id.personFab)
        val fabSubBtn2 = findViewById<FloatingActionButton>(R.id.pendingFab)
        val fabSubBtn3 = findViewById<FloatingActionButton>(R.id.addtoinventoryFab)
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
                45f,
                0f,
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

            if (isRotated) {
                floatingActionButton.startAnimation(reverseAnim)
                isRotated = false
                isRotated = false
                fabSubBtn1.visibility = View.GONE
                fabSubBtn2.visibility = View.GONE
                fabSubBtn3.visibility = View.GONE

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
            } else {
                floatingActionButton.startAnimation(anim)
                fabSubBtn1.visibility = View.VISIBLE
                fabSubBtn2.visibility = View.VISIBLE
                fabSubBtn3.visibility = View.VISIBLE
                fabSubBtn1.startAnimation(subButtonAnimationVertical)
                fabSubBtn2.startAnimation(subButtonAnimationDiagonal)
                fabSubBtn3.startAnimation(subButtonAnimationHorizontal)
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
                var donationCount = 0
                for (dataSnapshot in snapshot.children) {
                    val product = dataSnapshot.getValue(SellUploadClass::class.java)!!
                    if(product.isBought == "yes") {
                        donationCount++
                    }
                    if (product.isBought == "no" && product.sellerId != currentUserId) {
                        productList.add(product)
                    }
                }

                donationInfoTxt.text = "Total Donations using FindIt: $donationCount"
                planeLoad.visibility = View.GONE

                adapter = ProductAdapter(this@MainActivity, productList)
                recyclerView.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                planeLoad.visibility = View.GONE
            }
        })
    }
}
