package com.example.finalyearprojectdm

import GroupChatAdapter
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
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finalyearprojectdm.databinding.ActivityItineraryDetailsBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
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

    private var currentUserProfileImageId: Int = R.drawable.baseline_add_24

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

        if (itinerary != null) {
            Log.d(TAG, "Itinerary ID: ${itinerary.id}")
        }


        /*
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
        */


        // Display the itinerary title and details
        binding.titleTextView.setText(itinerary?.title)
        binding.descriptionTextView.text = itinerary?.description

        fetchCurrentUserProfileImageId()

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

        val sendToChatButton: Button = findViewById(R.id.send_to_chat_button)
        sendToChatButton.setOnClickListener {
            val itinerary = intent.getSerializableExtra("itinerary") as? Itinerary
            if (itinerary != null) {
                showGroupChatsBottomSheet(itinerary)
            } else {
                Toast.makeText(this, "Itinerary details not found", Toast.LENGTH_SHORT).show()
            }
        }

        val editItineraryButton: Button = findViewById(R.id.edit_itinerary_button)
        editItineraryButton.setOnClickListener {
            val itinerary = intent.getSerializableExtra("itinerary") as? Itinerary
            if (itinerary != null) {
                showEditItineraryDialog(itinerary)
            } else {
                Toast.makeText(this, "Itinerary details not found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //update the title of an itinerary in Firestore.
    private fun updateItineraryTitle(itinerary: Itinerary, newTitle: String) {
        val user = firebaseAuth.currentUser
        if (user != null) {
            val itinerariesRef = firestore.collection("users")
                .document(user.uid)
                .collection("itineraries")
                .document(itinerary.id.toString())
            itinerariesRef.update("title", newTitle)
                .addOnSuccessListener {
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

    // shows a bottom sheet with a list of group chats that the user can send the itinerary too
    private fun showGroupChatsBottomSheet(itinerary: Itinerary) {
        val bottomSheetDialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_group_chats, null)
        bottomSheetDialog.setContentView(view)

        val recyclerView: RecyclerView = view.findViewById(R.id.group_chats_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize an empty list for group chats
        val groupChats = mutableListOf<GroupChat>()
        val groupChatAdapter = GroupChatAdapter(groupChats) { groupChat ->
            sendItineraryToGroupChat(itinerary, groupChat)
            bottomSheetDialog.dismiss()
        }
        recyclerView.adapter = groupChatAdapter

        // Load group chats from Firestore
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance().collection("groupChats")
            .whereArrayContains("userIds", userId)
            .get()
            .addOnSuccessListener { documents ->
                val fetchedGroupChats = documents.map { document ->
                    document.toObject(GroupChat::class.java).copy(id = document.id)
                }
                // Update the adapter with the fetched group chats
                groupChatAdapter.updateGroupChats(fetchedGroupChats)
            }
            .addOnFailureListener { e ->
                // Handle the error here
                Toast.makeText(this, "Failed to load group chats: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }

        bottomSheetDialog.show()
    }

    private fun sendItineraryToGroupChat(itinerary: Itinerary, groupChat: GroupChat) {
        // Convert the itinerary to a message format that can be sent to the group chat
        val message = ChatMessage(
            text = itinerary.title,
            senderId = FirebaseAuth.getInstance().currentUser?.uid ?: "",
            itineraryTitle = itinerary.title,
            itineraryDescription = itinerary.description,
            itineraryId = itinerary.id.toString(),
            imageResourceId = currentUserProfileImageId // Use the fetched image ID
        )

        // Send the message to the Firestore collection for the selected group chat
        FirebaseFirestore.getInstance().collection("groupChats").document(groupChat.id)
            .collection("chatMessages").add(message)
            .addOnSuccessListener {
                // Show a toast with the Itinerary ID included
                Toast.makeText(this, "Itinerary sent to group chat successfully, ID: ${itinerary.id}", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to send itinerary to group chat", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Error sending itinerary to group chat", e)
            }
    }

    private fun fetchCurrentUserProfileImageId() {
        val userId = firebaseAuth.currentUser?.uid ?: return // Early return if user ID is null
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    currentUserProfileImageId = documentSnapshot.getLong("imageResourceId")?.toInt()
                        ?: R.drawable.baseline_add_24
                }
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error getting user profile image ID: ", e)
            }
    }


    private fun showEditItineraryDialog(itinerary: Itinerary) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_itinerary, null)
        val titleEditText: EditText = dialogView.findViewById(R.id.edit_itinerary_title)
        val daysRecyclerView: RecyclerView = dialogView.findViewById(R.id.RecyclerView1)
        val commentsRecyclerView: RecyclerView = dialogView.findViewById(R.id.comments_recycler_view)
        val saveButton: Button = dialogView.findViewById(R.id.save_changes_button)

        titleEditText.setText(itinerary.title)

        // Setup RecyclerView for days
        val daysAdapter = DayAdapter(itinerary.days.toMutableList()) { updatedDay ->
            updateDayInFirebase(itinerary.id, updatedDay, itinerary.days)
        }
        daysRecyclerView.layoutManager = LinearLayoutManager(this)
        daysRecyclerView.adapter = daysAdapter

        // Setup RecyclerView for comments
        commentsRecyclerView.layoutManager = LinearLayoutManager(this)
        val commentsAdapter = CommentsAdapter(mutableListOf())  // Assuming CommentsAdapter exists
        commentsRecyclerView.adapter = commentsAdapter
        loadComments(itinerary.id, commentsAdapter) // Assuming loadComments method exists

        // AlertDialog setup
        val alertDialog = AlertDialog.Builder(this)
            .setTitle("Edit Itinerary Details")
            .setView(dialogView)
            .setNegativeButton("Cancel", null)
            .create()

        saveButton.setOnClickListener {
            val newTitle = titleEditText.text.toString().trim()
            if (itinerary.title != newTitle || daysAdapter.hasChanges) {
                updateItinerary(itinerary.id, newTitle, daysAdapter.days)
                updateItineraryDescription(itinerary.id, daysAdapter.days)  // Ensure description is updated too
            }
            alertDialog.dismiss()
        }

        alertDialog.show()
    }




    private fun updateItinerary(itineraryId: String, newTitle: String, days: List<DayItinerary>) {
        // Update the whole itinerary including the combined description
        val combinedDescription = days.sortedBy { it.dayNumber }
            .joinToString("\n") { "Day ${it.dayNumber}: ${it.description}" }
        val updates = mapOf("title" to newTitle, "description" to combinedDescription)

        firestore.collection("users").document(firebaseAuth.currentUser!!.uid)
            .collection("itineraries").document(itineraryId)
            .update(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Itinerary updated successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error updating itinerary: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }



    private fun loadComments(itineraryId: String, adapter: CommentsAdapter) {
        val userId = firebaseAuth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId)
                .collection("itineraries").document(itineraryId)
                .collection("comments")
                .get()
                .addOnSuccessListener { snapshot ->
                    val newComments = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Comment::class.java)
                    }
                    adapter.updateComments(newComments)  // Update your adapter with the new list
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error loading comments: ", e)
                }
        }
    }


    private fun updateDayInFirebase(itineraryId: String, updatedDay: DayItinerary, days: MutableList<DayItinerary>) {
        // Logic to update a single day in Firebase
        val dayRef = firestore.collection("users").document(firebaseAuth.currentUser!!.uid)
            .collection("itineraries").document(itineraryId)
            .collection("days").document("Day ${updatedDay.dayNumber}")

        dayRef.update("description", updatedDay.description)
            .addOnSuccessListener {
                Log.d(TAG, "Day updated successfully in Firestore.")
                // Update the corresponding day in the days list with the updatedDay
                val index = days.indexOfFirst { it.dayNumber == updatedDay.dayNumber }
                if (index != -1) {
                    days[index] = updatedDay
                }
                updateItineraryDescription(itineraryId, days)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error updating day in Firestore.", e)
            }
    }

    private fun updateItineraryDescription(itineraryId: String, days: MutableList<DayItinerary>) {
        val combinedDescription = days.sortedBy { it.dayNumber }
            .joinToString("\n") { "Day ${it.dayNumber}: ${it.description}" }

        val itineraryRef = firestore.collection("users").document(firebaseAuth.currentUser?.uid ?: "")
            .collection("itineraries").document(itineraryId)

        itineraryRef.update("description", combinedDescription)
            .addOnSuccessListener {
                Toast.makeText(this, "Itinerary description updated successfully.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error updating itinerary description.", Toast.LENGTH_SHORT).show()
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