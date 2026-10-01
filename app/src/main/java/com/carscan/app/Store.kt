package com.carscan.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class Saved(val plate: String, val model: String)

class Store(ctx: Context) {
    private val p = ctx.getSharedPreferences("carscan", Context.MODE_PRIVATE)

    fun read(key: String): List<Saved> = try {
        val a = JSONArray(p.getString(key, "[]"))
        (0 until a.length()).map {
            val o = a.getJSONObject(it)
            Saved(o.optString("p"), o.optString("m"))
        }
    } catch (e: Exception) {
        emptyList()
    }

    fun write(key: String, list: List<Saved>) {
        val a = JSONArray()
        list.forEach { a.put(JSONObject().put("p", it.plate).put("m", it.model)) }
        p.edit().putString(key, a.toString()).apply()
    }
}
