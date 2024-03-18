package com.assignment.findit

import SellUploadClass
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ProductAdapter(val context: Context, val productList: ArrayList<SellUploadClass>) :
    RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productImage: ImageView = itemView.findViewById(R.id.imageView2)
        val productName: TextView = itemView.findViewById(R.id.tv1)
        val price: TextView = itemView.findViewById(R.id.textView4)
        val phno: TextView = itemView.findViewById(R.id.textView5)
        val location: TextView = itemView.findViewById(R.id.textView6)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.main_page_all_sold_card, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]

        // Set product details to the views (replace with your logic)
        //holder.productImage.setImageResource(R.drawable.your_product_image) // Replace with image loading logic based on product data
        holder.productName.text = "Seller is: "+product.sellerName
        holder.price.text =
            product.price.toString() // Assuming you have description fields in Product class
        holder.phno.text =product.phno
        holder.location.text = "Location: "+product.location // Assuming you have a price field in Product class
    }

    override fun getItemCount(): Int {
        return productList.size
    }
}
