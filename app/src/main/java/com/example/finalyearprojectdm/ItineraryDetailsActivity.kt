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
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.finalyearprojectdm.databinding.ActivityItineraryDetailsBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
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

        val destinationAirportCode = itinerary?.airportCode

        val client = OkHttpClient()

        val request = Request.Builder()
            .url("https://tripadvisor16.p.rapidapi.com/api/v1/flights/searchFlights?sourceAirportCode=DUB&destinationAirportCode=LHR&date=2024-06-07&itineraryType=ONE_WAY&sortOrder=ML_BEST_VALUE&numAdults=1&numSeniors=0&classOfService=ECONOMY&pageNumber=1&currencyCode=USD")
            .get()
            .addHeader("X-RapidAPI-Key", "a24edb7afcmsh52645be6fd3c50dp17e1aejsn9a1ef81f7bf2")
            .addHeader("X-RapidAPI-Host", "tripadvisor16.p.rapidapi.com")
            .build()

        Log.d("API_REQUEST_PROGRESS", "Request built, ready to send")

        GlobalScope.launch(Dispatchers.IO) {
            try {
                Log.d("API_REQUEST_PROGRESS", "Sending request")
                val response = client.newCall(request).execute()
                Log.d("API_REQUEST_PROGRESS", "Request sent, received response")

                val responseBody = response.body?.string()

                val gson = Gson()
                val apiResponse = gson.fromJson(responseBody, ApiResponse::class.java)

                withContext(Dispatchers.Main) {
                    Log.d("API_REQUEST_PROGRESS", "Updating UI with response")
                    if (apiResponse.data.flights.isNotEmpty()) {
                        val flight = apiResponse.data.flights[0]
                        if (flight.segments.isNotEmpty()) {
                            val segment = flight.segments[0]
                            if (segment.legs.isNotEmpty()) {
                                val leg = segment.legs[0]
                                val flightInfo = "Origin: ${leg.originStationCode}\n" +
                                        "Destination: ${leg.destinationStationCode}\n" +
                                        "Departure Time: ${leg.departureDateTime}\n" +
                                        "Arrival Time: ${leg.arrivalDateTime}\n"
                                        //"Purchase Link: ${flight.purchaseLinks[0].url}"    LINK TO FLIGHT PURCHASE
                                binding.flightInfoTextView.text = flightInfo
                            }
                        }
                    }
                    Log.d("API_REQUEST_PROGRESS", "UI updated")
                }
            } catch (e: Exception) {
                Log.e("API_REQUEST_ERROR", "Failed to execute request", e)
            }
        }


        // Display the itinerary title and details
        binding.titleTextView.setText(itinerary?.title)
        binding.descriptionTextView.text = itinerary?.description


        binding.titleTextView.setOnClickListener {
            if (itinerary != null) {
                // Create an EditText for the dialog
                val editText = EditText(this)
                editText.setText(itinerary.title)

                // Create the AlertDialog
                val alertDialog = AlertDialog.Builder(this)
                    .setTitle("Edit Itinerary Title")
                    .setView(editText)
                    .setPositiveButton("Save", null)
                    .setNegativeButton("Cancel") { dialog, _ ->
                        dialog.cancel()
                    }
                    .create()

                alertDialog.setOnShowListener {
                    val positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    positiveButton.setOnClickListener {
                        val newTitle = editText.text.toString().trim()
                        if (newTitle.isNotBlank()) {
                            updateItineraryTitle(itinerary, newTitle)
                            alertDialog.dismiss()
                        }
                    }

                    // Change button color
                    positiveButton.setTextColor(Color.RED)
                    val negativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                    negativeButton.setTextColor(Color.BLUE)
                }

                alertDialog.show()
            }
        }
    }

    private fun updateItineraryTitle(itinerary: Itinerary, newTitle: String) {
        val user = firebaseAuth.currentUser
        if (user != null) {
            val itinerariesRef = firestore.collection("users")
                .document(user.uid)
                .collection("itineraries")
                .document(itinerary.id.toString())
            itinerariesRef.update("title", newTitle)
                .addOnSuccessListener {
                    // Update the TextView and the itinerary object
                    binding.titleTextView.text = newTitle
                    itinerary.title = newTitle
                    Toast.makeText(this, "Title updated successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error updating document", e)
                    Toast.makeText(this, "Error updating title", Toast.LENGTH_SHORT).show()
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