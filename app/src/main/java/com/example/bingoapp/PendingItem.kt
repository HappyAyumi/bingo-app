package com.example.bingoapp

data class PendingItem(
    val reason: String,
    val points: Int,
    val cellIndex: Int,
    val taskName: String,
    val timestamp: Long
)
