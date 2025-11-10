package com.example.bingoapp

import android.content.Context
import android.net.Uri
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

    private val photos = mutableMapOf<Int, String?>()

    inner class BingoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textMission: TextView = view.findViewById(R.id.missionText)
        val imagePhoto: ImageView = view.findViewById(R.id.missionImage)

        init {
            view.isClickable = true
            view.isFocusable = true

            // ✅ マスをタップしたときにイベント発火
            view.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(position)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BingoViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_mission, parent, false)
        return BingoViewHolder(view)
    }

    override fun onBindViewHolder(holder: BingoViewHolder, position: Int) {
        holder.textMission.text = missions[position]

        val photoPath = photos[position]
        if (photoPath != null) {
            holder.imagePhoto.setImageURI(Uri.parse(photoPath))
            holder.imagePhoto.visibility = View.VISIBLE
        } else {
            holder.imagePhoto.visibility = View.INVISIBLE
        }
    }

    override fun getItemCount(): Int = missions.size

    fun updatePhoto(index: Int, path: String) {
        photos[index] = path
        notifyItemChanged(index)
    }
}
