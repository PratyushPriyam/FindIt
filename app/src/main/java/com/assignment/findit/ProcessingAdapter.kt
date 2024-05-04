package com.assignment.findit

import SellUploadClass
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProcessingAdapter(val context: Context, val productList: ArrayList<SellUploadClass>) :
    RecyclerView.Adapter<PendingViewHolder>() {
    val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PendingViewHolder {
        val view: View = LayoutInflater.from(context)
            .inflate(R.layout.pending_product_item_layout, parent, false)
        return PendingViewHolder(view)
    }

    override fun onBindViewHolder(holder: PendingViewHolder, position: Int) {
        val product = productList[position]

        // Fetch buyer details based on boughtBy field in product
        val buyerRef = database.getReference("FindIt/UserDetails").child(product.boughtBy)
        buyerRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val buyerMap = snapshot.value as HashMap<String, Any>
                    val buyerName = buyerMap["name"]?.toString() ?: ""
                    val buyerLocation = buyerMap["address"]?.toString() ?: "" // Assuming address is stored under "address" field
                    val buyerPhoneNo = buyerMap["phone"]?.toString() ?: ""

                    // Donations Done count (assuming unique product IDs in "sell" node)
                    val sellerDonationsRef = database.getReference("FindIt/users/${product.boughtBy}/sell")
                    sellerDonationsRef.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            var donationsDone = 0
                            for (dataSnapshot in snapshot.children) {
                                // Assuming each child represents a product with a unique ID
                                donationsDone++
                            }
                            holder.donationsDoneTxt.text = "Donations Done: $donationsDone"
                        }

                        override fun onCancelled(error: DatabaseError) {
                            // Handle database errors here
                        }
                    })

                    // Donations Received count (assuming "boughtBy" field for tracking)
                    val donationsReceivedRef = database.getReference("FindIt/allSold/")
                    donationsReceivedRef.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            var donationsReceived = 0
                            for (dataSnapshot in snapshot.children) {
                                val soldProduct = dataSnapshot.getValue(SellUploadClass::class.java)!!
                                if (soldProduct.boughtBy == product.boughtBy) {
                                    donationsReceived++
                                }
                            }
                            holder.donationsTakenTxt.text = "Donations Received: $donationsReceived"
                        }

                        override fun onCancelled(error: DatabaseError) {
                            // Handle database errors here
                        }
                    })

                    holder.sellerNameTv.text = "Requested By: $buyerName"
                    holder.locationTv.text = "Location: $buyerLocation"
                    holder.phoneNoTv.text = "Phone Number: $buyerPhoneNo"

                    // Rest of the data binding (product name, price) remains the same
                    holder.productNameTv.text = "Item Name: " + product.productName
                    holder.priceTv.text = product.qty.toString()

                    // Handle button visibility and functionality based on isBought
                    if (product.isBought == "pending") {
                        holder.agreeBtn.visibility = View.VISIBLE
                        holder.leaveBtn.visibility = View.VISIBLE
                        // ... (existing code for button clicks)
                    } else {
                        holder.agreeBtn.visibility = View.GONE
                        holder.leaveBtn.visibility = View.GONE
                    }
                } else {
                    // Handle case where buyer data is not found
                    Toast.makeText(
                        context, "Buyer information not found!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database errors here
            }
        })
    }

    private fun handleAgreeAction(product: SellUploadClass) {
        val reference = database.getReference("FindIt/allSold").child(product.uid)

        reference.setValue(product.apply {
            isBought = "yes"
        })
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Update buyer's "buy" node to include the product information (optional, implement based on your data structure)
                    removeFromPending(product) // Delete from seller's pending

                    // (Optional) Show a success message or update UI
                } else {
                    // Handle update failure (e.g., log the error)
                }
            }
    }

    private fun handleLeaveAction(product: SellUploadClass) {
        val reference = database.getReference("FindIt/allSold").child(product.uid) // Assuming uid is unique identifier for the product in "allSold"

        reference.setValue(product.apply {
            isBought = "no"
            boughtBy = ""
            // Set other relevant fields back to their original values (if needed)
        })
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    removeFromPending(product) // Delete from seller's pending

                    // (Optional) Show a confirmation message or update UI
                } else {
                    // Handle update failure (e.g., log the error)
                }
            }
    }

    private fun removeFromPending(product: SellUploadClass) {
        val sellerId = FirebaseAuth.getInstance().currentUser!!.uid
        val pendingRef = database.getReference("FindIt/users/$sellerId/pending").child(product.uid) // Assuming uid is used in pending node as well
        pendingRef.removeValue()
    }

    override fun getItemCount(): Int {
        return productList.size
    }
}

class PendingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val sellerNameTv: TextView = itemView.findViewById(R.id.sellerNameTv)
    val productNameTv: TextView = itemView.findViewById(R.id.productNameTv)
    val locationTv: TextView = itemView.findViewById(R.id.locationTv)
    val priceTv: TextView = itemView.findViewById(R.id.priceTv)
    val phoneNoTv: TextView = itemView.findViewById(R.id.phoneNoTv)
    val agreeBtn: AppCompatButton = itemView.findViewById(R.id.buttonAgree)
    val leaveBtn: AppCompatButton = itemView.findViewById(R.id.buttonLeave)
    val donationsTakenTxt: TextView = itemView.findViewById(R.id.donationsTakenTxt)
    val donationsDoneTxt: TextView = itemView.findViewById(R.id.donationsDoneTxt)
}
