package com.example.finalyearprojectdm

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
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
                        Itinerary().apply {
                            title = doc.getString("title") ?: ""
                            description = doc.getString("description") ?: ""
                        }
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

    private fun setupRecyclerView(itineraries: List<Itinerary>) {
        adapter = StoredProposalsAdapter(itineraries) { itinerary ->
            // Handle item click here
        }
        recyclerView.adapter = adapter
    }
}
