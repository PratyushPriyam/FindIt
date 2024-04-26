package com.assignment.findit

import SellUploadClass
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ProductAdapter(val context: Context, val productList: ArrayList<SellUploadClass>) :
    RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productImage: ImageView = itemView.findViewById(R.id.imageView2)
        val productName: TextView = itemView.findViewById(R.id.tv1)
        val price: TextView = itemView.findViewById(R.id.textView4)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.main_page_all_sold_card_list, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]

        // Set product details to the views (replace with your logic)
        //holder.productImage.setImageResource(R.drawable.your_product_image) // Replace with image loading logic based on product data
        holder.productName.text = "Sold By: "+product.sellerName
        holder.price.text = "Price: "
        Glide.with(context)
            .load(product.imageUrl)  // Load image URL from SellUploadClass
            .into(holder.productImage)
        holder.itemView.setOnClickListener {
            navigateToProductDetails(product) // Call a new method to handle click
        }
    }

    private fun navigateToProductDetails(product: SellUploadClass) {
        val intent = Intent(context, SingleProductSell::class.java)
        intent.putExtra("productimg", product.imageUrl) // Replace with actual image logic
        intent.putExtra("soldby", product.sellerName)
        intent.putExtra("location", product.location)
        intent.putExtra("sellerid", product.sellerId)
        intent.putExtra("phno", product.phno)
        intent.putExtra("uid", product.uid)
        context.startActivity(intent)
    }

    override fun getItemCount(): Int {
        return productList.size
    }
}
