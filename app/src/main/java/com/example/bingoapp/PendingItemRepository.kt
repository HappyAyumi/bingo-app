package com.example.bingoapp

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject

object PendingItemRepository {

    private const val PREF_NAME = "pending_items_pref"
    private const val KEY_PENDING = "pending_items"

    fun addPending(
        context: Context,
        reason: String,
        points: Int,
        cellIndex: Int,
        taskName: String
    ) {
        try {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val jsonStr = prefs.getString(KEY_PENDING, "[]") ?: "[]"

            val jsonArray = JSONArray(jsonStr)

            val newItem = JSONObject().apply {
                put("reason", reason)
                put("points", points)
                put("cellIndex", cellIndex)   // ★追加
                put("taskName", taskName)     // ★追加
                put("timestamp", System.currentTimeMillis())
            }

            jsonArray.put(newItem)
            prefs.edit().putString(KEY_PENDING, jsonArray.toString()).apply()

            Log.d("PendingRepo", "保存成功: $newItem")
        } catch (e: Exception) {
            Log.e("PendingRepo", "保存失敗: ${e.message}")
        }
    }


    fun getPendingList(context: Context): List<PendingItem> {
        return try {
            val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val jsonStr = prefs.getString(KEY_PENDING, "[]") ?: "[]"

            val jsonArray = JSONArray(jsonStr)
            val list = mutableListOf<PendingItem>()

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    PendingItem(
                        reason = obj.getString("reason"),
                        points = obj.getInt("points"),
                        cellIndex = obj.getInt("cellIndex"),   // ★追加
                        taskName = obj.getString("taskName"),  // ★追加
                        timestamp = obj.getLong("timestamp")
                    )
                )
            }
            list
        } catch (e: Exception) {
            Log.e("PendingRepo", "読み込み失敗: ${e.message}")
            emptyList()
        }
    }

    fun clear(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_PENDING, "[]").apply()
    }
}