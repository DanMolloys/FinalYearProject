package com.example.finalyearprojectdm

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class LocationAdapter(private val locations: List<String>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var isExpanded = false // Tracks expansion state

    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val locationHeader: TextView = view.findViewById(R.id.locationTitle)
        val detailsContainer: LinearLayout = view.findViewById(R.id.detailsContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeaderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_location_header, parent, false)
        return HeaderViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as HeaderViewHolder).locationHeader.text = "Locations"
        holder.locationHeader.setOnClickListener {
            isExpanded = !isExpanded
            holder.detailsContainer.visibility = if (isExpanded) View.VISIBLE else View.GONE
            if (isExpanded) {
                holder.detailsContainer.removeAllViews()
                locations.forEach { location ->
                    val detailView = LayoutInflater.from(holder.itemView.context).inflate(R.layout.item_location_detail, holder.detailsContainer, false) as TextView
                    detailView.text = location
                    holder.detailsContainer.addView(detailView)
                }
            } else {
                holder.detailsContainer.removeAllViews()
            }
        }
    }

    override fun getItemCount(): Int = 1 // Only one item for header
}

