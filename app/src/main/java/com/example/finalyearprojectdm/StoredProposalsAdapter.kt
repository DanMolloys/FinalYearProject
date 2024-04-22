package com.example.finalyearprojectdm

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.finalyearprojectdm.Itinerary

class StoredProposalsAdapter(
    private val itineraries: List<Itinerary>,
    private val onClickListener: (Itinerary) -> Unit,
    private var selectionMode: Boolean,
    private val selectedItems: MutableList<Itinerary>,
    private val onTwoItemsSelected: () -> Unit,
    private val activity: StoredProposalsActivity

) : RecyclerView.Adapter<StoredProposalsAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.itinerary_title)
        val flagImageView: ImageView = view.findViewById(R.id.flag_image_view)
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

        itinerary.startingLocation?.let {
            val countryCode = activity.getCountryCode(it)
            countryCode?.let { code ->
                activity.loadFlagIntoImageView(holder.flagImageView, code)
            }
        }

        // Set the click listener on the holder.itemView, which refers to the entire CardView
        holder.itemView.setOnClickListener {
            if (selectionMode) {
                toggleSelection(itinerary, holder)
            } else {
                Log.d("AdapterClick", "Item clicked: ${itinerary.title}")
                onClickListener(itinerary)
            }
        }
    }


    private fun toggleSelection(itinerary: Itinerary, holder: ViewHolder) {
        if (selectedItems.contains(itinerary)) {
            selectedItems.remove(itinerary)
            holder.itemView.isSelected = false
        } else {
            if (selectedItems.size < 2) {
                selectedItems.add(itinerary)
                holder.itemView.isSelected = true
            }
        }
        if (selectedItems.size == 2) {
            onTwoItemsSelected()
            selectionMode = false
            notifyDataSetChanged() // Refresh the list to clear or show selection
        }
    }

    override fun getItemCount() = itineraries.size
}