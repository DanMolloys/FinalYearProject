package com.example.finalyearprojectdm

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CompareProposals : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compare_proposals)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""

        // Retrieve itineraries from intent
        val selectedItem1 = intent.getSerializableExtra("selectedItem1") as Itinerary
        val selectedItem2 = intent.getSerializableExtra("selectedItem2") as Itinerary

        // Setup text views to display titles of each itinerary
        val textView1 = findViewById<TextView>(R.id.TextView1)
        val textView2 = findViewById<TextView>(R.id.TextView2)
        textView1.text = selectedItem1.title
        textView2.text = selectedItem2.title

        // Setup RecyclerViews for displaying days
        // Setup RecyclerViews for displaying days
        val recyclerView1 = findViewById<RecyclerView>(R.id.RecyclerView1)
        val recyclerView2 = findViewById<RecyclerView>(R.id.RecyclerView2)
        recyclerView1.layoutManager = LinearLayoutManager(this)
        recyclerView2.layoutManager = LinearLayoutManager(this)

        // Pass a no-op lambda since editing is not required in this context
        recyclerView1.adapter = DayAdapter(selectedItem1.days.sortedBy { it.dayNumber }.toMutableList()) { /* no operation */ }
        recyclerView2.adapter = DayAdapter(selectedItem2.days.sortedBy { it.dayNumber }.toMutableList()) { /* no operation */ }



        // Setup RecyclerViews for displaying locations
        val breakdown1 = findViewById<RecyclerView>(R.id.breakdown1)
        val breakdown2 = findViewById<RecyclerView>(R.id.breakdown2)
        breakdown1.layoutManager = LinearLayoutManager(this)
        breakdown2.layoutManager = LinearLayoutManager(this)
        val locations1 = getLocationsFromItinerary(selectedItem1)
        val locations2 = getLocationsFromItinerary(selectedItem2)
        breakdown1.adapter = LocationAdapter(locations1)
        breakdown2.adapter = LocationAdapter(locations2)
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
                    location = location.split(" ")[0]
                    if (!seenLocations.contains(location)) {
                        seenLocations.add(location)
                        locations.add(location)
                    }
                }
            }
        }
        return locations
    }

    // Inflating the options menu for navigation
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_back_menu, menu)
        return true
    }

    // Handling menu item selections
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.home -> {
                startActivity(Intent(this, MainActivity::class.java))
                return true
            }

            R.id.back -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
