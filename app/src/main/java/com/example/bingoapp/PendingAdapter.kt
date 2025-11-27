package com.example.bingoapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PendingAdapter(
    private val items: MutableList<PendingItem>,
    private val onApprove: (PendingItem) -> Unit,
    private val onReject: (PendingItem) -> Unit
) : RecyclerView.Adapter<PendingAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val reasonText: TextView = view.findViewById(R.id.reasonText)
        val pointsText: TextView = view.findViewById(R.id.pointsText)
        val approveBtn: Button = view.findViewById(R.id.approveButton)
        val rejectBtn: Button = view.findViewById(R.id.rejectButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pending, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.reasonText.text = item.reason
        holder.pointsText.text = "${item.points} pt"

        holder.approveBtn.setOnClickListener {
            onApprove(item)
        }

        holder.rejectBtn.setOnClickListener {
            onReject(item)
        }
    }

    override fun getItemCount(): Int = items.size

    fun removeItem(item: PendingItem) {
        val index = items.indexOf(item)
        if (index != -1) {
            items.removeAt(index)
            notifyItemRemoved(index)
        }
    }
}
