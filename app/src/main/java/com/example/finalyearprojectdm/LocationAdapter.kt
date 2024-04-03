package com.example.finalyearprojectdm

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class LocationAdapter(private val locations: List<String>) : RecyclerView.Adapter<LocationAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val locationCard: CardView = view.findViewById(R.id.locationCard)
        val locationText: TextView = view.findViewById(R.id.location_item_text)
        var isLocationVisible = false
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_location, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val location = locations[position]

        if (position == 0) {
            holder.locationText.text = "Locations"
            holder.locationCard.setOnClickListener {
                holder.isLocationVisible = !holder.isLocationVisible
                notifyDataSetChanged() // This will trigger a re-bind of all items, showing or hiding the locations
            }
        } else {
            holder.locationText.text = location
            // Change visibility based on isLocationVisible
            holder.locationText.visibility = if (holder.isLocationVisible) View.VISIBLE else View.GONE
        }
    }

    override fun getItemCount(): Int = locations.size
}