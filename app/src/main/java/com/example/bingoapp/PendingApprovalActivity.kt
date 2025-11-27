package com.example.bingoapp

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class PendingApprovalActivity : AppCompatActivity() {

    private lateinit var emptyTextView: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PendingAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pending_approval)

        emptyTextView = findViewById(R.id.emptyTextView)
        recyclerView = findViewById(R.id.pendingRecyclerView)

        loadPendingList()
    }

    private fun loadPendingList() {
        val pendingList = PendingItemRepository.getPendingList(this).toMutableList()

        if (pendingList.isEmpty()) {
            emptyTextView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
            return
        }

        emptyTextView.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE

        adapter = PendingAdapter(
            items = pendingList,
            onApprove = { item -> approveItem(item) },
            onReject = { item -> rejectItem(item) }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun approveItem(item: PendingItem) {
        adapter.removeItem(item)

        Toast.makeText(this, "承認しました", Toast.LENGTH_SHORT).show()

        removeFromRepository(item)
        checkEmptyState()
    }

    private fun rejectItem(item: PendingItem) {
        adapter.removeItem(item)

        Toast.makeText(this, "却下しました", Toast.LENGTH_SHORT).show()

        removeFromRepository(item)
        checkEmptyState()
    }

    private fun checkEmptyState() {
        if (adapter.itemCount == 0) {
            emptyTextView.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        }
    }

    private fun removeFromRepository(item: PendingItem) {
        val current = PendingItemRepository.getPendingList(this).toMutableList()
        current.removeAll { it.timestamp == item.timestamp }

        val prefs = getSharedPreferences("pending_items_pref", MODE_PRIVATE)
        val jsonArray = org.json.JSONArray()

        for (p in current) {
            val obj = org.json.JSONObject()
            obj.put("reason", p.reason)
            obj.put("points", p.points)
            obj.put("timestamp", p.timestamp)
            jsonArray.put(obj)
        }

        prefs.edit().putString("pending_items", jsonArray.toString()).apply()
    }
}
