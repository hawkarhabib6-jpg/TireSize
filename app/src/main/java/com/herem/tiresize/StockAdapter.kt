package com.herem.tiresize

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StockAdapter(
    private val onDelete: (StockTire) -> Unit
) : RecyclerView.Adapter<StockAdapter.VH>() {

    private var items: List<StockTire> = emptyList()

    fun submit(list: List<StockTire>) {
        items = list
        notifyDataSetChanged()
    }

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val size: TextView = view.findViewById(R.id.stkSize)
        val note: TextView = view.findViewById(R.id.stkNote)
        val del: ImageButton = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_stock, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val t = items[position]
        holder.size.text = t.size
        holder.note.text = t.note
        holder.note.visibility = if (t.note.isBlank()) View.GONE else View.VISIBLE
        holder.del.setOnClickListener { onDelete(t) }
    }

    override fun getItemCount(): Int = items.size
}
