package com.assignment.findit

import SellUploadClass // Assuming your product data class
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class DonateAdapter(val context: Context, val productList: ArrayList<SellUploadClass>) :
    RecyclerView.Adapter<DonateAdapter.DonateViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DonateViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.donate_card, parent, false) // Assuming item layout ID
        return DonateViewHolder(view)
    }

    override fun onBindViewHolder(holder: DonateViewHolder, position: Int) {
        val product = productList[position]

        holder.productNameTv.text = product.sellerName
        holder.locationTv.text = "Location: ${product.location}"

        // Load product image using Glide (optional)
//        Glide.with(context).load(product.productImgUrl).into(holder.productIv)
    }

    override fun getItemCount(): Int {
        return productList.size
    }

    class DonateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        val productIv: ImageView = itemView.findViewById(R.id.donate_item_image) // Assuming ID
        val productNameTv: TextView = itemView.findViewById(R.id.donatename) // Assuming ID
        val locationTv: TextView = itemView.findViewById(R.id.donateloc) // Assuming ID
    }
}
