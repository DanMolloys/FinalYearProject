package com.example.finalyearprojectdm

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CompareProposals : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compare_proposals)


        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""

        val selectedItem1 = intent.getSerializableExtra("selectedItem1") as Itinerary
        val selectedItem2 = intent.getSerializableExtra("selectedItem2") as Itinerary

        // Get the TextViews
        val textView1 = findViewById<TextView>(R.id.TextView1)
        val textView2 = findViewById<TextView>(R.id.TextView2)

        // Set the text for the TextViews
        textView1.text = selectedItem1.title
        textView2.text = selectedItem2.title

        // Get the RecyclerViews
        val recyclerView1 = findViewById<RecyclerView>(R.id.RecyclerView1)
        val recyclerView2 = findViewById<RecyclerView>(R.id.RecyclerView2)

        // Set the layout managers for the RecyclerViews
        recyclerView1.layoutManager = LinearLayoutManager(this)
        recyclerView2.layoutManager = LinearLayoutManager(this)

        // Set the adapters for the RecyclerViews
        recyclerView1.adapter = DayAdapter(selectedItem1.days.sortedBy { it.dayNumber })
        recyclerView2.adapter = DayAdapter(selectedItem2.days.sortedBy { it.dayNumber })

        val breakdown1 = findViewById<RecyclerView>(R.id.breakdown1)
        val breakdown2 = findViewById<RecyclerView>(R.id.breakdown2)

        // Set the layout managers for the breakdown RecyclerViews
        breakdown1.layoutManager = LinearLayoutManager(this)
        breakdown2.layoutManager = LinearLayoutManager(this)

        val locations1 = getLocationsFromItinerary(selectedItem1).toMutableList()
        locations1.add(0, "Locations")
        breakdown1.adapter = LocationAdapter(locations1)

        val locations2 = getLocationsFromItinerary(selectedItem2).toMutableList()
        locations2.add(0, "Locations")
        breakdown2.adapter = LocationAdapter(locations1)

        breakdown1.adapter = LocationAdapter(locations1)
        breakdown2.adapter = LocationAdapter(locations2)

        //add any locations to this hash set, if location has already been stored don't add.
        val seenLocations = HashSet<String>()

        val locations = StringBuilder()
        for (day in selectedItem1.days) {
            val destinationIndex = day.description.indexOf("Destination: ")
            if (destinationIndex != -1) {
                val endOfLineIndex = day.description.indexOf("\n", destinationIndex)
                if (endOfLineIndex != -1) {
                    var location = day.description.substring(
                        destinationIndex + "Destination: ".length,
                        endOfLineIndex
                    )
                    location = location.split(" ")[0] // take the first word
                    if (seenLocations.contains(location)) continue // Skip this location if it's already been added
                    seenLocations.add(location) // Add the location to the set
                    locations.append("• $location\n")
                }
            }
        }


        for (day in selectedItem2.days) {
            val destinationIndex = day.description.indexOf("Destination: ")
            if (destinationIndex != -1) {
                val endOfLineIndex = day.description.indexOf("\n", destinationIndex)
                if (endOfLineIndex != -1) {
                    var location = day.description.substring(
                        destinationIndex + "Destination: ".length,
                        endOfLineIndex
                    )
                    location = location.split(" ")[0] // take the first word
                    if (seenLocations.contains(location)) continue // Skip this location if it's already been added
                    seenLocations.add(location) // Add the location to the set
                    locations.append("• $location\n")
                }
            }
        }

    }

    private fun getLocationsFromItinerary(itinerary: Itinerary): List<String> {
        val seenLocations = HashSet<String>()
        val locations = mutableListOf<String>()
        for (day in itinerary.days) {
            val destinationIndex = day.description.indexOf("Destination: ")
            if (destinationIndex != -1) {
                val endOfLineIndex = day.description.indexOf("\n", destinationIndex)
                if (endOfLineIndex != -1) {
                    var location = day.description.substring(
                        destinationIndex + "Destination: ".length,
                        endOfLineIndex
                    )
                    location = location.split(" ")[0] // take the first word
                    if (seenLocations.contains(location)) continue // Skip this location if it's already been added
                    seenLocations.add(location) // Add the location to the set
                    locations.add(location)
                }
            }
        }
        return locations
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_back_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.home -> {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                true
            }

            R.id.back -> {
                val intent = Intent(this, StoredProposalsActivity::class.java)
                startActivity(intent)
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }
}