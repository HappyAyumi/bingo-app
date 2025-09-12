package com.example.bingoapp

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class BingoAdapter(
    private val missions: List<String>,
    private val onCellClick: (Int) -> Unit
) : RecyclerView.Adapter<BingoAdapter.BingoViewHolder>() {

    private val images = mutableMapOf<Int, Uri?>()   // マスごとの写真URIを保存
    private val completed = mutableMapOf<Int, Boolean>() // 達成状態を保存

    inner class BingoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textMission: TextView = view.findViewById(R.id.textMission)
        val imageMission: ImageView = view.findViewById(R.id.imageMission)
        val container: LinearLayout = view as LinearLayout

        init {
            view.setOnClickListener {
                // マスがタップされたら「達成/未達成」を切り替え
                val current = completed[adapterPosition] ?: false
                completed[adapterPosition] = !current
                notifyItemChanged(adapterPosition)

                onCellClick(adapterPosition) // 外にも通知（必要なら）
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

        val uri = images[position]
        if (uri != null) {
            holder.imageMission.visibility = View.VISIBLE
            holder.imageMission.setImageURI(uri)
        } else {
            holder.imageMission.visibility = View.GONE
        }

        // 背景切り替え（未達成 → モノトーン / 達成 → ピンク）
        val isDone = completed[position] ?: false
        if (isDone || uri != null) {
            holder.container.setBackgroundResource(R.drawable.bingo_cell_complete)
        } else {
            holder.container.setBackgroundResource(R.drawable.bingo_cell_incomplete)
        }
    }

    override fun getItemCount() = missions.size

    fun setImage(position: Int, uri: Uri) {
        images[position] = uri
        completed[position] = true // 写真を追加したら自動的に達成扱い
        notifyItemChanged(position)
    }
}
