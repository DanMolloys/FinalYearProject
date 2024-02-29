package com.example.finalyearprojectdm

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import com.example.finalyearprojectdm.databinding.ActivityProfileupdateBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileUpdateActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileupdateBinding
    private lateinit var imageAdapter: ImageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileupdateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get the drawable items
        val imgs = resources.obtainTypedArray(R.array.profile_pics)
        imageAdapter = ImageAdapter(this, imgs)
        binding.gridView.adapter = imageAdapter

        binding.gridView.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val imageResourceId = imgs.getResourceId(position, -1)
                saveImageToFirestore(imageResourceId)
                binding.selectedImageView.setImageResource(imageResourceId)
            }

        binding.homeButton.setOnClickListener {
            finish() // Assuming you want to close the current activity and go back
        }

        // Get a Firestore instance
        val db = FirebaseFirestore.getInstance()
        // Get the user ID of the currently logged in user
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Fetch the user document
        val docRef = db.collection("users").document(userId)
        docRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document != null && document.exists()) {
                    Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                    val firstName = document.getString("first")

                    // Update the TextViews
                    binding.firstnameTextView.text = firstName

                    // Check if the document contains the imageResourceId field
                    document.getLong("imageResourceId")?.toInt()?.let { imageResourceId ->
                        // Set the saved image
                        binding.selectedImageView.setImageResource(imageResourceId)
                    }
                } else {
                    Log.d(TAG, "No such document")
                }
            } else {
                Log.d(TAG, "get failed with ", task.exception)
            }
        }
    }

    private fun saveImageToFirestore(imageResourceId: Int) {
        // Get a Firestore instance
        val db = FirebaseFirestore.getInstance()
        // Get the user ID of the currently logged in user
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Create a map with the imageResourceId
        val user = hashMapOf<String, Any>("imageResourceId" to imageResourceId)

        // Update the document with the user ID
        db.collection("users").document(userId)
            .update(user as Map<String, Any>) // Explicit cast to Map<String, Any>
            .addOnSuccessListener {
                Log.d(TAG, "DocumentSnapshot successfully updated!")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error updating document", e)
            }
    }
}