package com.herem.tiresize

data class Car(
    val brand: String,
    val model: String,
    val yearFrom: Int,
    val yearTo: Int,
    val sizes: List<String>,
    val aliases: List<String>
) {
    val yearRange: String get() = "$yearFrom - $yearTo"

    fun matches(query: String): Boolean {
        if (query.isBlank()) return true
        val q = query.trim().lowercase()
        val words = q.split(Regex("\\s+"))
        return words.all { w ->
            val year = w.toIntOrNull()
            if (year != null && year in 1980..2035) {
                year in yearFrom..yearTo
            } else {
                brand.lowercase().contains(w) ||
                model.lowercase().contains(w) ||
                aliases.any { it.lowercase().contains(w) }
            }
        }
    }
}
