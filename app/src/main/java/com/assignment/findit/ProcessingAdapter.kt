package com.assignment.findit

import SellUploadClass
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

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

        holder.sellerNameTv.text = "Sold By: ${product.sellerName}"
        holder.productNameTv.text = product.productName
        holder.locationTv.text = "Location: ${product.location}"
        holder.priceTv.text = "Price: $${product.price}"
        holder.phoneNoTv.text = "Phone Number: ${product.phno}"

        // Handle button visibility and functionality based on isBought
        if (product.isBought == "pending") {
            holder.agreeBtn.visibility = View.VISIBLE
            holder.leaveBtn.visibility = View.VISIBLE

            holder.agreeBtn.setOnClickListener {
                handleAgreeAction(product)
            }

            holder.leaveBtn.setOnClickListener {
                handleLeaveAction(product)
            }
        } else {
            holder.agreeBtn.visibility = View.GONE
            holder.leaveBtn.visibility = View.GONE
        }
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
}
