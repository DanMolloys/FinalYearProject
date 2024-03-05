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
    private val onClickListener: (Itinerary) -> Unit,
    private var selectionMode: Boolean,
    private val selectedItems: MutableList<Itinerary>,
    private val onTwoItemsSelected: () -> Unit

) : RecyclerView.Adapter<StoredProposalsAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.itinerary_title)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_itinerary, parent, false) // replace with your actual layout id
        return ViewHolder(view)
    }

    fun setSelectionMode(mode: Boolean) {
        this.selectionMode = mode
        notifyDataSetChanged() // this will refresh your list
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val itinerary = itineraries[position]
        holder.title.text = itinerary.title
        holder.itemView.setOnClickListener {
            if (selectionMode) {
                if (selectedItems.size < 2) {
                    selectedItems.add(itinerary)
                    if (selectedItems.size == 2) {
                        onTwoItemsSelected()
                        selectionMode = false
                        notifyDataSetChanged() // refresh the list to clear the selection
                    }
                }
            } else {
                onClickListener(itinerary)
                val intent = Intent(it.context, ItineraryDetailsActivity::class.java)
                intent.putExtra("itinerary", itinerary)
                it.context.startActivity(intent)
            }
        }
    }

    override fun getItemCount() = itineraries.size
}