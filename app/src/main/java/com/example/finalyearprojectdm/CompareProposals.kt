package com.example.finalyearprojectdm

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
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