package com.example.finalyearprojectdm

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finalyearprojectdm.databinding.ActivityStoredproposalsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class StoredProposalsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStoredproposalsBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: StoredProposalsAdapter

    private var selectionMode = false
    private var selectedItems = mutableListOf<Itinerary>()

    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStoredproposalsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""

        loadItineraries()

        val btnCompare = findViewById<Button>(R.id.btnCompare)
        btnCompare.setOnClickListener {
            selectionMode = true
            selectedItems.clear()
            adapter.setSelectionMode(selectionMode) // update selectionMode in the adapter
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.reg_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.goMain -> {
                val intent = Intent (this, MainActivity ::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun loadItineraries() {
        val user = firebaseAuth.currentUser
        if (user != null) {
            firestore.collection("users").document(user.uid).collection("itineraries")
                .get()
                .addOnSuccessListener { result ->
                    val itineraries = result.documents.map { doc ->
                        val itinerary = Itinerary().apply {
                            title = doc.getString("title") ?: ""
                            description = doc.getString("description") ?: ""
                        }
                        // Retrieve the day-by-day itinerary
                        firestore.collection("users").document(user.uid).collection("itineraries").document(doc.id).collection("days")
                            .get()
                            .addOnSuccessListener { result ->
                                val days = result.documents.map { dayDoc ->
                                    DayItinerary().apply {
                                        dayNumber = dayDoc.getLong("dayNumber")?.toInt() ?: 0
                                        description = dayDoc.getString("description") ?: ""
                                    }
                                }
                                itinerary.days = days.toMutableList()
                            }
                        itinerary
                    }
                    setupRecyclerView(itineraries)
                }
                .addOnFailureListener { e ->
                    // Handle the error here
                }
        } else {
            // No user is logged in
        }
    }

    private fun showConfirmDialog() {
        if (selectedItems.size != 2) return

        val builder = AlertDialog.Builder(this)
        builder.setMessage("You have selected: \n-${selectedItems[0].title}\n-${selectedItems[1].title}")
        val dialog = builder.create()
        dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Confirm") { dialogInterface, which ->
            val intent = Intent(this, CompareProposals::class.java)
            // Pass the selected items into the intent
            intent.putExtra("selectedItem1", selectedItems[0])
            intent.putExtra("selectedItem2", selectedItems[1])

            startActivity(intent)
        }
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel") { dialogInterface, which ->
            dialogInterface.dismiss()
        }
        dialog.show()

        val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        val negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)

        positiveButton.setTextColor(Color.RED)
        negativeButton.setTextColor(Color.BLUE)
    }

    private fun setupRecyclerView(itineraries: List<Itinerary>) {
        adapter = StoredProposalsAdapter(
            itineraries,
            { itinerary ->
                // Handle item click here
            },
            selectionMode,
            selectedItems,
            ::showConfirmDialog
        )
        recyclerView.adapter = adapter
    }
}

