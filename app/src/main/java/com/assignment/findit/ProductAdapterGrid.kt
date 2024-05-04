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

class ProductAdapterGrid(val context: Context, val productList: ArrayList<SellUploadClass>) :
    RecyclerView.Adapter<ProductAdapterGrid.ProductViewHolder>() {

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productImage: ImageView = itemView.findViewById(R.id.imageViewGrid)
        val price: TextView = itemView.findViewById(R.id.tvGridPrice)
        val qty: TextView = itemView.findViewById(R.id.gridName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.main_page_all_sold_card_grid, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]
        holder.price.text = "Quantity: " + product.qty
        holder.qty.text = "Seller: " + product.sellerName
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
        intent.putExtra("qty", product.qty)
        context.startActivity(intent)
    }

    override fun getItemCount(): Int {
        return productList.size
    }
}
