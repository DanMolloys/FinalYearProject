package com.example.finalyearprojectdm

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.finalyearprojectdm.Itinerary

class StoredProposalsAdapter(
    private val itineraries: List<Itinerary>,
    private val onClickListener: (Itinerary) -> Unit
) : RecyclerView.Adapter<StoredProposalsAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.itinerary_title)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_itinerary, parent, false) // replace with your actual layout id
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val itinerary = itineraries[position]
        holder.title.text = itinerary.title
        holder.itemView.setOnClickListener {
            onClickListener(itinerary)
            val intent = Intent(it.context, ItineraryDetailsActivity::class.java)
            intent.putExtra("itinerary", itinerary)
            it.context.startActivity(intent)
        }
    }

    override fun getItemCount() = itineraries.size
}