package com.example.bingoapp

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class BingoAdapter(
    private val cells: List<BingoCell>,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<BingoAdapter.CellViewHolder>() {

    inner class CellViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.cellText)
        val imageView: ImageView = view.findViewById(R.id.cellImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CellViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bingo_cell, parent, false)
        return CellViewHolder(view)
    }

    override fun onBindViewHolder(holder: CellViewHolder, position: Int) {
        val cell = cells[position]
        holder.textView.text = cell.text
        if (cell.imageUri != null) {
            Glide.with(holder.imageView.context)
                .load(cell.imageUri)
                .into(holder.imageView)
        } else {
            holder.imageView.setImageResource(android.R.color.transparent)
        }

        holder.itemView.setOnClickListener { onItemClick(position) }
    }

    override fun getItemCount(): Int = cells.size
}