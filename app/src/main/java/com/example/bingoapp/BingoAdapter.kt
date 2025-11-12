package com.example.bingoapp

import android.content.Context
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

    // 各マスに対応する画像URIを保持（nullの場合は未撮影）
    private val imageUris = MutableList<Uri?>(missions.size) { null }

    inner class BingoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.imageView)
        val textView: TextView = view.findViewById(R.id.textView)

        init {
            view.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(position)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BingoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_bingo, parent, false)
        return BingoViewHolder(view)
    }

    override fun onBindViewHolder(holder: BingoViewHolder, position: Int) {
        holder.textView.text = missions[position]

        val uri = imageUris[position]
        if (uri != null) {
            Glide.with(holder.itemView.context)
                .load(uri)
                .centerCrop()
                .into(holder.imageView)
        } else {
            // ⚠ ic_add_photo が存在しない環境でもビルド可能なように修正
            holder.imageView.setImageResource(android.R.drawable.ic_menu_camera)
        }
    }

    override fun getItemCount(): Int = missions.size

    /** 撮影 or 選択した画像を保存して表示 */
    fun setImage(position: Int, uri: Uri) {
        imageUris[position] = uri
        notifyItemChanged(position)
    }

    /** 指定マスに画像があるかチェック */
    fun hasImage(position: Int): Boolean {
        return imageUris[position] != null
    }
}
