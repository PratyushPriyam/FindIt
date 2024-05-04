package com.assignment.findit

import SellUploadClass
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class BoughtProductAdapter(val context: Context, val productList: ArrayList<SellUploadClass>) :
    RecyclerView.Adapter<ProductViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view: View = LayoutInflater.from(context)
            .inflate(R.layout.buy_custom_layout, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]

        holder.sellerNameTv.text = "Donated By: ${product.sellerName}"
        holder.productNameTv.text = "Product Name: "+product.productName
        holder.qty_tv.text = "Quantity: " + product.qty
        holder.phoneNoTv.text = "Phone Number: ${product.phno}"
        // Load image using Glide
        Glide.with(context)
            .load(product.imageUrl)  // Load image URL from SellUploadClass
            .into(holder.productIv)
    }

    override fun getItemCount(): Int {
        return productList.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (productList[position].isBoughtByCurrentUser) 1 else 0
    }
}

class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val productIv: ImageView = itemView.findViewById(R.id.product_image)
    val sellerNameTv: TextView = itemView.findViewById(R.id.seller_name_tv)
    val productNameTv: TextView = itemView.findViewById(R.id.product_name_tv)
    val qty_tv: TextView = itemView.findViewById(R.id.qty_tv)
    val phoneNoTv: TextView = itemView.findViewById(R.id.phone_no_tv)
}
