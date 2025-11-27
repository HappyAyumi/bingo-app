package com.example.bingoapp

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bingoapp.databinding.ActivityPendingApprovalBinding

class PendingApprovalActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPendingApprovalBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPendingApprovalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadPendingItems()

        binding.approveButton.setOnClickListener {
            approveAll()
        }
    }

    private fun loadPendingItems() {
        val items = PendingItemRepository.getPendingItems(this)

        if (items.isEmpty()) {
            binding.emptyTextView.visibility = View.VISIBLE
        } else {
            binding.emptyTextView.visibility = View.GONE
        }

        val adapter = PendingAdapter(this, items)
        binding.pendingListView.adapter = adapter
    }

    private fun approveAll() {
        val total = PendingItemRepository.approveAll(this)
        Toast.makeText(this, "合計 $total pt を承認しました", Toast.LENGTH_SHORT).show()
        finish()
    }
}
