package com.example.bingoapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BingoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.bingoRecyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 4)

        // 仮の16マス分のデータ
        val items = MutableList(16) { BingoItem("マス${it+1}", null) }
        adapter = BingoAdapter(items, this)
        recyclerView.adapter = adapter
    }
}
