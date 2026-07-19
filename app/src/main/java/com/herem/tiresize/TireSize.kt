package com.herem.tiresize

object TireSize {
    // Matches 215/55R17, 215/55/17, 215-55-17, 215 55 17, 215/55 R17 ...
    private val RE = Regex("""(\d{3})\s*[/\-\s]\s*(\d{2})\s*[/\-\srR]*\s*(\d{2})""")

    /** Returns a canonical key like "215/55R17", or null if the text isn't a tire size. */
    fun normalize(text: String): String? {
        val m = RE.find(text.trim()) ?: return null
        val (w, a, d) = m.destructured
        return "$w/${a}R$d"
    }

    fun isSizeQuery(text: String): Boolean = normalize(text) != null
}
