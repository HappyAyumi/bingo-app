package com.example.bingoapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import coil.load

data class PendingItem(
    val reason: String,
    val points: Int,
    val imageUri: String
)

class PendingAdapter(context: Context, private val items: List<PendingItem>) :
    ArrayAdapter<PendingItem>(context, 0, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val item = items[position]
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_pending, parent, false)

        val imageView = view.findViewById<ImageView>(R.id.imageView)
        val reasonText = view.findViewById<TextView>(R.id.reasonText)
        val pointsText = view.findViewById<TextView>(R.id.pointsText)

        // Coilで画像読み込み
        imageView.load(item.imageUri) {
            crossfade(true)
        }

        reasonText.text = item.reason
        pointsText.text = "+${item.points} pt"

        return view
    }
}
