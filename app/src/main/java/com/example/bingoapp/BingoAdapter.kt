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

    // 各マスの画像状態を保存
    private val photos = mutableMapOf<Int, String?>()

    inner class BingoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val missionText: TextView = view.findViewById(R.id.missionText)
        val missionImage: ImageView = view.findViewById(R.id.missionImage)

        init {
            // ✅ マスをタップしたときにカメラ起動（MainActivity に通知）
            view.setOnClickListener {
                val position = adapterPosition
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
        holder.missionText.text = missions[position]

        val photoPath = photos[position]
        if (photoPath != null) {
            holder.missionImage.setImageURI(Uri.parse(photoPath))
            holder.missionImage.visibility = View.VISIBLE
            holder.missionText.alpha = 0.5f
        } else {
            holder.missionImage.setImageResource(R.drawable.placeholder_image)
            holder.missionText.alpha = 1f
        }
    }

    override fun getItemCount(): Int = missions.size

    // ✅ カメラ撮影後に画像を更新
    fun updatePhoto(index: Int, path: String) {
        photos[index] = path
        notifyItemChanged(index)
    }
}
