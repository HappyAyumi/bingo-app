package com.example.bingoapp

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BingoAdapter(
    private val context: Context,
    private val missions: List<String>
) : RecyclerView.Adapter<BingoAdapter.BingoViewHolder>() {

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

        val prefs = context.getSharedPreferences("bingo_prefs", Context.MODE_PRIVATE)
        val photoPath = prefs.getString("photo_$position", null)

        if (photoPath != null) {
            val bitmap = BitmapFactory.decodeFile(photoPath)
            holder.image.setImageBitmap(bitmap)
            holder.text.alpha = 0.3f
        } else {
            holder.image.setImageResource(R.drawable.placeholder_image)
            holder.text.alpha = 1f
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, CameraActivity::class.java)
            intent.putExtra("MISSION_INDEX", position)
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = missions.size
}
