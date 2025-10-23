package com.example.bingoapp

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BingoAdapter(
    private val context: Context,
    private val missions: List<String>,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<BingoAdapter.BingoViewHolder>() {

    private val photoPaths = MutableList<String?>(missions.size) { null }

    init {
        val prefs = context.getSharedPreferences("bingo_prefs", Context.MODE_PRIVATE)
        missions.indices.forEach { i ->
            photoPaths[i] = prefs.getString("photo_$i", null)
        }
    }

    class BingoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val text: TextView = view.findViewById(R.id.missionText)
        val image: ImageView = view.findViewById(R.id.missionImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BingoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mission, parent, false)
        return BingoViewHolder(view)
    }

    override fun onBindViewHolder(holder: BingoViewHolder, position: Int) {
        val mission = missions[position]
        holder.text.text = mission

        val path = photoPaths[position]
        if (path != null) {
            val bitmap = BitmapFactory.decodeFile(path)
            holder.image.setImageBitmap(bitmap)
            holder.text.alpha = 0.4f
        } else {
            holder.image.setImageResource(R.drawable.placeholder_image)
            holder.text.alpha = 1f
        }

        holder.itemView.setOnClickListener { onItemClick(position) }
    }

    override fun getItemCount() = missions.size

    // ✅ 即時更新用関数
    fun updatePhoto(position: Int, path: String) {
        photoPaths[position] = path
        notifyItemChanged(position)
    }
}
