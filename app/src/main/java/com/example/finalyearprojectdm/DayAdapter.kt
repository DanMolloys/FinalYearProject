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
    private val changedDayIndices = mutableSetOf<Int>()  // Set to track indices of changed days

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

        holder.dayDescription.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                hasChanges = true
            }

            override fun afterTextChanged(s: Editable?) {
                val adapterPos = holder.adapterPosition
                if (adapterPos != RecyclerView.NO_POSITION) {
                    days[adapterPos].description = s.toString()
                    changedDayIndices.add(adapterPos)  // Add this day's index to changed indices
                }
            }
        })

        holder.dayDescription.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val updatedText = holder.dayDescription.text.toString()
                if (days[holder.adapterPosition].description != updatedText) {
                    days[holder.adapterPosition].description = updatedText
                    onDayUpdated(days[holder.adapterPosition])
                    hasChanges = true
                }
            }
        }

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



