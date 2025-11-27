package com.example.bingoapp

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object PendingItemRepository {

    private const val PREF_NAME = "PendingApproval"
    private const val KEY_PENDING = "pending_items"

    /** 承認待ちに追加 */
    fun addPending(context: Context, reason: String, points: Int, imageUri: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val listStr = prefs.getString(KEY_PENDING, "[]") ?: "[]"

        val array = JSONArray(listStr)

        val obj = JSONObject().apply {
            put("reason", reason)
            put("points", points)
            put("imageUri", imageUri)
        }

        array.put(obj)
        prefs.edit().putString(KEY_PENDING, array.toString()).apply()
    }

    /** PendingItem のリストを返す（Adapter 用） */
    fun getPendingItems(context: Context): List<PendingItem> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val listStr = prefs.getString(KEY_PENDING, "[]") ?: "[]"

        val array = JSONArray(listStr)
        val result = mutableListOf<PendingItem>()

        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            result.add(
                PendingItem(
                    reason = obj.getString("reason"),
                    points = obj.getInt("points"),
                    imageUri = obj.optString("imageUri", "")
                )
            )
        }

        return result
    }

    /** 全承認（合計ポイント返す） */
    fun approveAll(context: Context): Int {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val listStr = prefs.getString(KEY_PENDING, "[]") ?: "[]"

        val array = JSONArray(listStr)
        var total = 0

        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            total += obj.getInt("points")
        }

        prefs.edit().putString(KEY_PENDING, "[]").apply()
        return total
    }
}
