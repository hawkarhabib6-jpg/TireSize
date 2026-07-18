package com.herem.tiresize

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: CarAdapter
    private lateinit var allCars: List<Car>
    private lateinit var txtCount: TextView
    private lateinit var edtSearch: EditText
    private lateinit var btnOnline: MaterialButton
    private lateinit var aiCard: CardView
    private lateinit var aiTitle: TextView
    private lateinit var aiSizes: TextView
    private lateinit var aiNote: TextView

    private val prefs by lazy { getSharedPreferences("tiresize", Context.MODE_PRIVATE) }
    private var searching = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        allCars = CarRepository.load(this)

        adapter = CarAdapter()
        val recycler = findViewById<RecyclerView>(R.id.recycler)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        txtCount = findViewById(R.id.txtCount)
        edtSearch = findViewById(R.id.edtSearch)
        btnOnline = findViewById(R.id.btnOnline)
        aiCard = findViewById(R.id.aiCard)
        aiTitle = findViewById(R.id.aiTitle)
        aiSizes = findViewById(R.id.aiSizes)
        aiNote = findViewById(R.id.aiNote)

        edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                aiCard.visibility = View.GONE
                filter(s?.toString() ?: "")
            }
        })

        btnOnline.setOnClickListener { startOnlineSearch() }
        btnOnline.setOnLongClickListener { askApiKey(force = true); true }

        filter("")
    }

    private fun filter(query: String) {
        val result = allCars.filter { it.matches(query) }
        adapter.submit(result)
        txtCount.text = getString(R.string.result_count, result.size)
        btnOnline.visibility = if (query.isBlank()) View.GONE else View.VISIBLE
    }

    private fun startOnlineSearch() {
        if (searching) return
        val query = edtSearch.text.toString().trim()
        if (query.isEmpty()) return

        val key = prefs.getString("api_key", "") ?: ""
        if (key.isEmpty()) {
            askApiKey(force = false)
            return
        }

        searching = true
        aiCard.visibility = View.GONE
        btnOnline.isEnabled = false
        btnOnline.text = getString(R.string.searching_online)

        Thread {
            try {
                val r = OnlineSearch.search(key, query)
                runOnUiThread {
                    if (r.found) showAiResult(r)
                    else Toast.makeText(this, R.string.not_identified, Toast.LENGTH_LONG).show()
                    finishSearch()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, R.string.online_error, Toast.LENGTH_LONG).show()
                    finishSearch()
                }
            }
        }.start()
    }

    private fun finishSearch() {
        searching = false
        btnOnline.isEnabled = true
        btnOnline.text = getString(R.string.online_search)
    }

    private fun showAiResult(r: AiResult) {
        aiTitle.text = "${r.brand} ${r.model}  (${r.years})"
        aiSizes.text = r.sizes.joinToString("\n")
        val disclaimer = getString(R.string.ai_disclaimer)
        aiNote.text = if (r.note.isNotBlank()) "${r.note}\n\n$disclaimer" else disclaimer
        aiCard.visibility = View.VISIBLE
    }

    private fun askApiKey(force: Boolean) {
        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        input.hint = getString(R.string.api_key_hint)
        if (force) input.setText(prefs.getString("api_key", "") ?: "")

        AlertDialog.Builder(this)
            .setTitle(R.string.api_key_title)
            .setMessage(R.string.api_key_message)
            .setView(input)
            .setPositiveButton(R.string.save) { _, _ ->
                val key = input.text.toString().trim()
                prefs.edit().putString("api_key", key).apply()
                if (key.isNotEmpty() && !force) startOnlineSearch()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}
