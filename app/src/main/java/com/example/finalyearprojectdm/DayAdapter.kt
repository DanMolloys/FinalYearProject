package com.example.finalyearprojectdm

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DayAdapter(private val days: List<DayItinerary>) : RecyclerView.Adapter<DayAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dayNumber: TextView = view.findViewById(R.id.dayNumber)
        val dayDescription: TextView = view.findViewById(R.id.dayDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.day_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val day = days[position]
        holder.dayNumber.text = "Day ${day.dayNumber}"
        holder.dayDescription.text = day.description

        // Set the description visibility to GONE by default
        holder.dayDescription.visibility = View.GONE

        // Set a click listener on the day number text view
        holder.dayNumber.setOnClickListener {
            // Toggle the description visibility when the day number is clicked
            if (holder.dayDescription.visibility == View.GONE) {
                holder.dayDescription.visibility = View.VISIBLE
            } else {
                holder.dayDescription.visibility = View.GONE
            }
        }
    }

    override fun getItemCount(): Int = days.size
}