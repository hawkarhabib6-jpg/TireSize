package com.herem.tiresize

import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.net.HttpURLConnection
import java.net.URL

data class AiResult(
    val found: Boolean,
    val brand: String,
    val model: String,
    val years: String,
    val sizes: List<String>,
    val note: String
)

object OnlineSearch {

    private const val ENDPOINT = "https://api.anthropic.com/v1/messages"
    private const val MODEL = "claude-sonnet-4-6"

    /** Runs a blocking HTTP request — call from a background thread only. */
    fun search(apiKey: String, query: String): AiResult {
        val prompt =
            "You are a tire fitment expert. The user searched for a car: \"" + query + "\". " +
            "Identify the car (brand, model, year or year range). If the query is ambiguous about generation, " +
            "use the most common generation sold in Iraq/Middle East. " +
            "Return ONLY a JSON object, no markdown, no explanation, with this exact shape: " +
            "{\"found\":true,\"brand\":\"...\",\"model\":\"...\",\"years\":\"2013-2018\"," +
            "\"sizes\":[\"215/55R17\"],\"note\":\"short note in Sorani Kurdish about trims, written in Arabic script\"} " +
            "sizes = the factory (OEM) tire sizes for that model/years, most common first. " +
            "If you cannot identify the car at all, return {\"found\":false}."

        val body = JSONObject()
            .put("model", MODEL)
            .put("max_tokens", 1000)
            .put("messages", JSONArray().put(
                JSONObject().put("role", "user").put("content", prompt)
            ))

        val conn = URL(ENDPOINT).openConnection() as HttpURLConnection
        try {
            conn.requestMethod = "POST"
            conn.connectTimeout = 20000
            conn.readTimeout = 60000
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("x-api-key", apiKey)
            conn.setRequestProperty("anthropic-version", "2023-06-01")

            conn.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }

            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val text = stream.bufferedReader().use(BufferedReader::readText)
            if (code !in 200..299) throw RuntimeException("HTTP $code: $text")

            val content = JSONObject(text).getJSONArray("content")
            val sb = StringBuilder()
            for (i in 0 until content.length()) {
                val block = content.getJSONObject(i)
                if (block.optString("type") == "text") sb.append(block.optString("text"))
            }
            val clean = sb.toString()
                .replace("```json", "")
                .replace("```", "")
                .trim()

            val r = JSONObject(clean)
            if (!r.optBoolean("found", false)) {
                return AiResult(false, "", "", "", emptyList(), "")
            }
            val sizes = ArrayList<String>()
            val sArr = r.optJSONArray("sizes") ?: JSONArray()
            for (i in 0 until sArr.length()) sizes.add(sArr.getString(i))
            return AiResult(
                found = sizes.isNotEmpty(),
                brand = r.optString("brand"),
                model = r.optString("model"),
                years = r.optString("years"),
                sizes = sizes,
                note = r.optString("note")
            )
        } finally {
            conn.disconnect()
        }
    }
}
