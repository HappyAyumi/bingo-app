package com.example.bingoapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BingoAdapter(private val items: List<BingoItem>) :
    RecyclerView.Adapter<BingoAdapter.BingoViewHolder>() {

    class BingoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.bingo_item_text)
        val imageView: ImageView = itemView.findViewById(R.id.bingo_item_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BingoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bingo, parent, false)
        return BingoViewHolder(view)
    }

    override fun onBindViewHolder(holder: BingoViewHolder, position: Int) {
        val item = items[position]
        if (item.text != null) {
            holder.textView.text = item.text
            holder.textView.visibility = View.VISIBLE
            holder.imageView.visibility = View.GONE
        } else if (item.imageResId != null) {
            holder.imageView.setImageResource(item.imageResId)
            holder.imageView.visibility = View.VISIBLE
            holder.textView.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = items.size
}
