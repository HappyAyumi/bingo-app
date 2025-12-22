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
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val jsonStr = prefs.getString(KEY_PENDING, "[]") ?: "[]"
        val jsonArray = JSONArray(jsonStr)

        val obj = JSONObject().apply {
            put("reason", reason)
            put("points", points)
            put("cellIndex", cellIndex)
            put("taskName", taskName)
            put("timestamp", System.currentTimeMillis())
        }

        jsonArray.put(obj)
        prefs.edit().putString(KEY_PENDING, jsonArray.toString()).apply()
    }

    fun getPendingList(context: Context): List<PendingItem> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val jsonStr = prefs.getString(KEY_PENDING, "[]") ?: "[]"
        val jsonArray = JSONArray(jsonStr)

        val list = mutableListOf<PendingItem>()
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)

            // ★ 欠けているデータはスキップ（壊れ防止）
            if (!obj.has("cellIndex") || !obj.has("taskName")) continue

            list.add(
                PendingItem(
                    reason = obj.getString("reason"),
                    points = obj.getInt("points"),
                    cellIndex = obj.getInt("cellIndex"),
                    taskName = obj.getString("taskName"),
                    timestamp = obj.getLong("timestamp")
                )
            )
        }
        return list
    }

    fun removeByTimestamp(context: Context, timestamp: Long) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val jsonArray = JSONArray(prefs.getString(KEY_PENDING, "[]") ?: "[]")
        val newArray = JSONArray()

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            if (obj.getLong("timestamp") != timestamp) {
                newArray.put(obj)
            }
        }

        prefs.edit().putString(KEY_PENDING, newArray.toString()).apply()
    }

    // ★ 一度だけ使うリセット用
    fun clear(context: Context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit().putString(KEY_PENDING, "[]").apply()
    }
}
