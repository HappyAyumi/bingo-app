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
    private val missions: List<String>,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<BingoAdapter.BingoViewHolder>() {

    private val imageUris = MutableList<Uri?>(missions.size) { null }

    inner class BingoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val missionText: TextView = itemView.findViewById(R.id.mission_text)
        val missionImage: ImageView = itemView.findViewById(R.id.mission_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BingoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.bingo_item_layout, parent, false)
        return BingoViewHolder(view)
    }

    override fun onBindViewHolder(holder: BingoViewHolder, position: Int) {
        holder.missionText.text = missions[position]
        holder.itemView.setOnClickListener { onItemClick(position) }

        val uri = imageUris[position]
        if (uri != null) {
            holder.missionImage.visibility = View.VISIBLE
            Glide.with(holder.itemView.context).load(uri).into(holder.missionImage)
        } else {
            holder.missionImage.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = missions.size

    fun setImage(position: Int, uri: Uri) {
        imageUris[position] = uri
        notifyItemChanged(position)
    }

    fun hasImage(position: Int): Boolean = imageUris[position] != null
}
