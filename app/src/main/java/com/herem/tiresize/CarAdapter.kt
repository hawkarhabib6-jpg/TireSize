package com.herem.tiresize

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CarAdapter : RecyclerView.Adapter<CarAdapter.VH>() {

    private var items: List<Car> = emptyList()

    fun submit(list: List<Car>) {
        items = list
        notifyDataSetChanged()
    }

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.txtTitle)
        val years: TextView = view.findViewById(R.id.txtYears)
        val sizes: TextView = view.findViewById(R.id.txtSizes)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_car, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val car = items[position]
        holder.title.text = "${car.brand} ${car.model}"
        holder.years.text = car.yearRange
        holder.sizes.text = car.sizes.joinToString("\n")
    }

    override fun getItemCount(): Int = items.size
}
