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
    private val onCellClick: (Int) -> Unit
) : RecyclerView.Adapter<BingoAdapter.BingoViewHolder>() {

    private val images = mutableMapOf<Int, Uri?>() // マスごとの写真URIを保存

    inner class BingoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textMission: TextView = view.findViewById(R.id.textMission)
        val imageMission: ImageView = view.findViewById(R.id.imageMission)

        init {
            view.setOnClickListener {
                onCellClick(adapterPosition) // マスがタップされたら通知
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BingoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bingo, parent, false)
        return BingoViewHolder(view)
    }

    override fun onBindViewHolder(holder: BingoViewHolder, position: Int) {
        holder.textMission.text = missions[position]

        // 正方形にする処理
        holder.itemView.post {
            val width = holder.itemView.width
            holder.itemView.layoutParams.height = width
        }

        val uri = images[position]
        if (uri != null) {
            holder.imageMission.visibility = View.VISIBLE
            holder.imageMission.setImageURI(uri)
        } else {
            holder.imageMission.visibility = View.GONE
        }
    }

    override fun getItemCount() = missions.size

    fun setImage(position: Int, uri: Uri) {
        images[position] = uri
        notifyItemChanged(position)
    }
}
