package com.example.finalyearprojectdm

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DayAdapter(val days: MutableList<DayItinerary>, private val onDayUpdated: (DayItinerary) -> Unit) : RecyclerView.Adapter<DayAdapter.ViewHolder>() {

    var hasChanges = false  // Property to track if any changes have been made

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dayNumber: TextView = view.findViewById(R.id.dayNumber)
        val dayDescription: EditText = view.findViewById(R.id.dayDescription)  // Change to EditText
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.day_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val day = days[position]
        holder.dayNumber.text = "Day ${day.dayNumber}"
        holder.dayDescription.setText(day.description)

        // Listener to detect changes and update the hasChanges flag
        holder.dayDescription.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val adapterPos = holder.adapterPosition
                if (adapterPos != RecyclerView.NO_POSITION) {
                    hasChanges = true  // Assuming you want to track changes
                }
            }

            override fun afterTextChanged(s: Editable?) {
                val adapterPos = holder.adapterPosition
                if (adapterPos != RecyclerView.NO_POSITION) {
                    days[adapterPos].description = s.toString()
                }
            }
        })


        // Save changes when focus is lost to update Firebase and handle UI
        holder.dayDescription.setOnFocusChangeListener { _, hasFocus ->
            val adapterPos = holder.adapterPosition
            if (!hasFocus && adapterPos != RecyclerView.NO_POSITION) {
                onDayUpdated(days[adapterPos])
            }
        }

        // Optional: Toggle visibility of EditText on click
        holder.dayNumber.setOnClickListener {
            val adapterPos = holder.adapterPosition
            if (adapterPos != RecyclerView.NO_POSITION) {
                val visibility = if (holder.dayDescription.visibility == View.GONE) View.VISIBLE else View.GONE
                holder.dayDescription.visibility = visibility
            }
        }
    }

    override fun getItemCount(): Int = days.size
}


