package com.example.finalyearprojectdm

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.finalyearprojectdm.databinding.ActivityItineraryDetailsBinding
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import com.google.gson.Gson

class ItineraryDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityItineraryDetailsBinding
    private val db = FirebaseFirestore.getInstance()

    private lateinit var toolbar: Toolbar

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityItineraryDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""

        val itinerary = intent.getSerializableExtra("itinerary") as? Itinerary

        // Display the itinerary details using the binding
        binding.titleTextView.setText(itinerary?.title)
        binding.descriptionTextView.text = itinerary?.description

        // Set the click listener
        binding.titleTextView.setOnClickListener {
            if (itinerary != null) {
                // what happens when clicked
            }
        }

        coroutineScope.launch {
            val flightInfoString = fetchFlights()
            val flightInfo = Gson().fromJson(flightInfoString, FlightInfo::class.java)
            updateUIWithFlightInfo(flightInfo)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_back_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.home -> {
                val intent = Intent (this, MainActivity ::class.java)
                startActivity(intent)
                true
            }
            R.id.back -> {
                val intent = Intent (this, StoredProposalsActivity ::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }


    private suspend fun fetchFlights(): String {
        return withContext(Dispatchers.IO) {
            val client = OkHttpClient()

            val request = Request.Builder()
                //hard coded london
                .url("https://tripadvisor16.p.rapidapi.com/api/v1/flights/searchAirport?query=london")
                .get()
                .addHeader("X-RapidAPI-Key", "a24edb7afcmsh52645be6fd3c50dp17e1aejsn9a1ef81f7bf2")
                .addHeader("X-RapidAPI-Host", "tripadvisor16.p.rapidapi.com")
                .build()

            val response = client.newCall(request).execute()
            return@withContext response.body?.string() ?: ""
        }
    }

    private fun updateUIWithFlightInfo(flightInfo: FlightInfo) {
        binding.flightInfoTextView.text = "Airport Name: ${flightInfo.airportName}" //, Airport Code: ${flightInfo.airportCode}"
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
    }
}