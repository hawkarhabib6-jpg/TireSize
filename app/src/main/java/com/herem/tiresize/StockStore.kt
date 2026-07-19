package com.herem.tiresize

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class StockTire(
    val id: Long,
    val size: String,
    val note: String
)

object StockStore {

    private const val PREFS = "tiresize"
    private const val KEY = "stock_tires"

    fun load(context: Context): MutableList<StockTire> {
        val raw = prefs(context).getString(KEY, "[]") ?: "[]"
        val arr = JSONArray(raw)
        val list = ArrayList<StockTire>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            list.add(StockTire(o.getLong("id"), o.getString("size"), o.optString("note")))
        }
        // newest first
        list.sortByDescending { it.id }
        return list
    }

    fun save(context: Context, list: List<StockTire>) {
        val arr = JSONArray()
        list.forEach {
            arr.put(JSONObject()
                .put("id", it.id)
                .put("size", it.size)
                .put("note", it.note))
        }
        prefs(context).edit().putString(KEY, arr.toString()).apply()
    }

    fun add(context: Context, size: String, note: String) {
        val list = load(context)
        list.add(StockTire(System.currentTimeMillis(), size, note))
        save(context, list)
    }

    fun delete(context: Context, id: Long) {
        val list = load(context).filter { it.id != id }
        save(context, list)
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
