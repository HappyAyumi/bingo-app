package com.example.bingoapp

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BingoAdapter(
    private val missions: List<String>,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<BingoAdapter.BingoViewHolder>() {

    private val imageUris = MutableList<Uri?>(missions.size) { null }

    class BingoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageMission: ImageView = itemView.findViewById(R.id.imageMission)
        val textMission: TextView = itemView.findViewById(R.id.textMission)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BingoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bingo, parent, false)
        return BingoViewHolder(view)
    }

    override fun onBindViewHolder(holder: BingoViewHolder, position: Int) {
        holder.textMission.text = missions[position]

        val uri = imageUris[position]
        if (uri != null) {
            holder.imageMission.setImageURI(uri)
            holder.imageMission.visibility = View.VISIBLE
        } else {
            // 画像がない場合は非表示
            holder.imageMission.visibility = View.GONE
        }

        holder.itemView.setOnClickListener {
            onItemClick(position)
        }
    }

    override fun getItemCount(): Int = missions.size

    fun hasImage(position: Int): Boolean {
        return imageUris[position] != null
    }

    fun setImage(position: Int, uri: Uri) {
        imageUris[position] = uri
        notifyItemChanged(position)
    }

    fun getImageUris(): List<Uri?> = imageUris

    fun getMissions(): List<String> = missions
}
