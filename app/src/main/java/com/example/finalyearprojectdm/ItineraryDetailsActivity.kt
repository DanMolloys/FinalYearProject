package com.example.finalyearprojectdm

import android.content.ContentValues.TAG
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
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class ItineraryDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityItineraryDetailsBinding

    private lateinit var firestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityItineraryDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""

        val itinerary = intent.getSerializableExtra("itinerary") as? Itinerary

        /*
        val client = OkHttpClient()

        val request = Request.Builder()
            .url("https://tripadvisor16.p.rapidapi.com/api/v1/flights/searchFlights?sourceAirportCode=DUB&destinationAirportCode=LHR&date=2024-03-06&itineraryType=ROUND_TRIP&sortOrder=best_flights&numAdults=1&numSeniors=0&classOfService=ECONOMY&pageNumber=1&currencyCode=USD")
            .get()
            .addHeader("X-RapidAPI-Key", "a24edb7afcmsh52645be6fd3c50dp17e1aejsn9a1ef81f7bf2")
            .addHeader("X-RapidAPI-Host", "tripadvisor16.p.rapidapi.com")
            .build()

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                withContext(Dispatchers.Main) {
                    binding.flightInfoTextView.text = responseBody
                }
            } catch (e: Exception) {
                Log.e("API_REQUEST_ERROR", "Failed to execute request", e)
            }
        }

         */

        // Display the itinerary title and details
        binding.titleTextView.setText(itinerary?.title)
        binding.descriptionTextView.text = itinerary?.description


        binding.titleTextView.setOnClickListener {
            if (itinerary != null) {
                // what happens when clicked
            }
        }
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