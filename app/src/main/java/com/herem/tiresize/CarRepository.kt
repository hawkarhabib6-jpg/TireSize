package com.herem.tiresize

import android.content.Context
import org.json.JSONArray

object CarRepository {

    private var cache: List<Car>? = null

    fun load(context: Context): List<Car> {
        cache?.let { return it }
        val json = context.assets.open("cars.json").bufferedReader().use { it.readText() }
        val arr = JSONArray(json)
        val list = ArrayList<Car>(arr.length())
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val sizes = ArrayList<String>()
            val sArr = o.getJSONArray("sizes")
            for (j in 0 until sArr.length()) sizes.add(sArr.getString(j))
            val aliases = ArrayList<String>()
            if (o.has("aliases")) {
                val aArr = o.getJSONArray("aliases")
                for (j in 0 until aArr.length()) aliases.add(aArr.getString(j))
            }
            list.add(
                Car(
                    brand = o.getString("brand"),
                    model = o.getString("model"),
                    yearFrom = o.getInt("yearFrom"),
                    yearTo = o.getInt("yearTo"),
                    sizes = sizes,
                    aliases = aliases
                )
            )
        }
        list.sortWith(compareBy({ it.brand }, { it.model }, { it.yearFrom }))
        cache = list
        return list
    }
}
