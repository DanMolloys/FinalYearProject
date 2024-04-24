package com.example.finalyearprojectdm

import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class ChatActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private var chatMessages = mutableListOf<ChatMessage>()

    private lateinit var toolbar: Toolbar

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private val PICK_IMAGE_REQUEST = 1

    private var itinerary: Itinerary? = null

    private var groupId: String? = null

    private var currentUserProfileImageId: Int = R.drawable.baseline_add_24

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        recyclerView = findViewById(R.id.chat_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        drawerLayout = findViewById(R.id.drawer_layout)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""

        fetchCurrentUserProfileImageId()

        // Initialize the chat adapter with a click listener for proposals
        chatAdapter = ChatAdapter(chatMessages, FirebaseAuth.getInstance().currentUser?.uid ?: "", onProposalClick = { chatMessage ->
            val bottomSheetDialog = BottomSheetDialog(this)
            val view = layoutInflater.inflate(R.layout.bottom_sheet_proposal, null)

            // Set the itinerary details in the bottom sheet
            val titleTextView = view.findViewById<TextView>(R.id.bottom_dialog_title_text_view)
            val descriptionTextView = view.findViewById<TextView>(R.id.bottom_dialog_description_text_view)
            titleTextView.text = chatMessage.itineraryTitle ?: "No title"
            descriptionTextView.text = chatMessage.itineraryDescription ?: "No description"

            val voteGreenButton = view.findViewById<AppCompatImageButton>(R.id.vote_green_button)
            val voteRedButton = view.findViewById<AppCompatImageButton>(R.id.vote_red_button)

            voteGreenButton.setOnClickListener {
                castVote(chatMessage.id, "green")
            }
            voteRedButton.setOnClickListener {
                castVote(chatMessage.id, "red")
            }
            view.findViewById<Button>(R.id.add_comment_button).setOnClickListener {
                val chatMessageId = chatMessage.id
                val itineraryId = chatMessage.itineraryId
                showAddCommentDialog(chatMessageId, itineraryId)
            }

            bottomSheetDialog.setContentView(view)
            bottomSheetDialog.show()
        })

        groupId = intent.getStringExtra("GROUP_ID")

        recyclerView.adapter = chatAdapter

        loadChatMessages()

        findViewById<Button>(R.id.send_message_button).setOnClickListener {
            val messageText = findViewById<EditText>(R.id.message_field).text.toString().trim()
            if (messageText.isEmpty()) {
                Toast.makeText(this, "Message cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val groupId = intent.getStringExtra("GROUP_ID") ?: return@setOnClickListener

            // Check if itinerary details are to be sent
            val message = itinerary?.let {
                ChatMessage(
                    text = messageText,
                    senderId = FirebaseAuth.getInstance().currentUser?.uid ?: "",
                    itineraryTitle = it.title,
                    itineraryDescription = it.description,
                    itineraryId = it.id.toString(),
                    imageResourceId = currentUserProfileImageId
                )
            } ?: ChatMessage(
                text = messageText,
                senderId = FirebaseAuth.getInstance().currentUser?.uid ?: "",
                imageResourceId = currentUserProfileImageId
            )

            // Send the message to Firestore
            FirebaseFirestore.getInstance().collection("groupChats").document(groupId)
                .collection("chatMessages").add(message)
                .addOnSuccessListener {
                    findViewById<EditText>(R.id.message_field).text.clear()
                    Toast.makeText(this, "Message sent", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to send message: ", e)
                    Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show()
                }
        }

        itinerary = intent.getSerializableExtra("itinerary") as? Itinerary
        if (intent.getBooleanExtra("send_itinerary", false) && itinerary != null) {
            sendItineraryProposal(itinerary!!)
        }

        val uploadImageButton = findViewById<Button>(R.id.upload_image_button)
        uploadImageButton.setOnClickListener {
            selectImage()
        }

        groupId = intent.getStringExtra("GROUP_ID")

        if (groupId != null) {
            loadChosenItineraryDetails(groupId!!)
        } else {
            Toast.makeText(this, "Error: Group ID is null.", Toast.LENGTH_SHORT).show()
        }

        toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()


    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }



    //loading chat messages from Firestore.
    //using snapshot so when vote is added, the UI will update automatically.
    private fun loadChatMessages() {
        val groupId = intent.getStringExtra("GROUP_ID") ?: return
        firestore.collection("groupChats").document(groupId)
            .collection("chatMessages")
            .orderBy("timestamp")
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    Log.w(TAG, "Listen failed.", firebaseFirestoreException)
                    return@addSnapshotListener
                }
                val newMessages = querySnapshot?.documents?.mapNotNull { document ->
                    document.toObject(ChatMessage::class.java)?.apply {
                        id = document.id
                        imageResourceId = document.getLong("imageResourceId")?.toInt() ?: R.drawable.baseline_add_24
                    }
                }.orEmpty()

                chatMessages.clear()
                chatMessages.addAll(newMessages)
                chatAdapter.notifyDataSetChanged()
            }
    }




    private fun fetchCurrentUserProfileImageId() {
        val userId = firebaseAuth.currentUser?.uid ?: return // Early return if user ID is null
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    // Here we update the currentUserProfileImageId with the one from Firestore
                    currentUserProfileImageId = documentSnapshot.getLong("imageResourceId")?.toInt()
                        ?: R.drawable.baseline_add_24
                }
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error getting user profile image ID: ", e)
            }
    }

    //send an itinerary proposal as a chat message.
    private fun sendItineraryProposal(itinerary: Itinerary) {
        if (itinerary.id.isNullOrEmpty()) {
            Log.e(TAG, "Itinerary ID is null or empty.")
            return
        }

        val message = ChatMessage(
            text = itinerary.title,
            senderId = FirebaseAuth.getInstance().currentUser?.uid ?: "",
            itineraryTitle = itinerary.title,
            itineraryDescription = itinerary.description,
            itineraryId = itinerary.id,
            imageResourceId = currentUserProfileImageId
        )

        val groupId = intent.getStringExtra("GROUP_ID") ?: return
        FirebaseFirestore.getInstance().collection("groupChats").document(groupId)
            .collection("chatMessages").add(message)
            .addOnSuccessListener {
                Toast.makeText(this, "Itinerary proposal sent successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to send itinerary proposal: ", e)
                Toast.makeText(this, "Failed to send itinerary proposal", Toast.LENGTH_SHORT).show()
            }
    }




    //maybe use Firebase Transactions?
    private fun castVote(messageId: String, vote: String) {
        val userId = firebaseAuth.currentUser?.uid ?: return
        val groupId = intent.getStringExtra("GROUP_ID") ?: return
        val messageRef = firestore.collection("groupChats").document(groupId)
            .collection("chatMessages").document(messageId)

        messageRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                val currentVotes = documentSnapshot["votes"] as? MutableMap<String, String> ?: mutableMapOf()

                // Set the user's vote to "green" or "red"
                currentVotes[userId] = vote

                // Update the document with the new vote
                messageRef.update("votes", currentVotes).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(this, "Vote updated successfully", Toast.LENGTH_SHORT).show()
                        checkIfAllApproved(messageId, currentVotes, groupId)
                    } else {
                        Toast.makeText(this, "Failed to update vote", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }.addOnFailureListener { e ->
            Log.e(TAG, "Failed to cast vote: ", e)
            Toast.makeText(this, "Failed to cast vote", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkIfAllApproved(messageId: String, votes: Map<String, String>, groupId: String) {
        // Here you will check if all users approved the itinerary
        // You'll need to fetch the user IDs of the group and compare with the votes map
        val groupChatRef = firestore.collection("groupChats").document(groupId)
        groupChatRef.get().addOnSuccessListener { documentSnapshot ->
            val userIds = documentSnapshot["userIds"] as? List<String> ?: return@addOnSuccessListener
            val allApproved = userIds.all { userId -> votes[userId] == "green" }

            if (allApproved) {
                // Set the itinerary as "approved" in your UI and Firestore
                Toast.makeText(this, "All users have approved the itinerary!", Toast.LENGTH_LONG).show()
                setApprovedItinerary(messageId, groupId)
            }
        }
    }

    private fun setApprovedItinerary(messageId: String, groupId: String) {
        // Logic to move the itinerary to the "chosenItineraries" subcollection
        val messageRef = firestore.collection("groupChats").document(groupId)
            .collection("chatMessages").document(messageId)
        val chosenItineraryRef = firestore.collection("groupChats").document(groupId)
            .collection("chosenItineraries").document(messageId)

        firestore.runTransaction { transaction ->
            val itinerary = transaction.get(messageRef).toObject(ChatMessage::class.java)
            itinerary?.let {
                transaction.set(chosenItineraryRef, it)
            }
            null
        }.addOnSuccessListener {
            // Update UI to show the chosen itinerary
            updateChosenItineraryUI(messageId)
        }
    }



    private fun updateChosenItineraryUI(messageId: String) {
        val groupId = intent.getStringExtra("GROUP_ID") ?: return

        firestore.collection("groupChats").document(groupId)
            .collection("chosenItineraries").document(messageId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val chosenItinerary = document.toObject(ChatMessage::class.java)
                    chosenItinerary?.let {
                        // Update the title and description TextViews
                        val titleTextView = findViewById<TextView>(R.id.itinerary_title)
                        val descriptionTextView = findViewById<TextView>(R.id.itinerary_description)

                        titleTextView.text = it.itineraryTitle ?: "No Title Available"
                        descriptionTextView.text = it.itineraryDescription ?: "No Description Available"

                        // Display the itinerary title in a Toast message
                        if (it.itineraryTitle != null) {
                            Toast.makeText(this, "Loaded Itinerary: ${it.itineraryTitle}", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    Log.d(TAG, "No chosen itinerary found with ID: $messageId")
                    Toast.makeText(this, "No chosen itinerary available.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error getting chosen itinerary: ", e)
                Toast.makeText(this, "Error loading itinerary.", Toast.LENGTH_SHORT).show()
            }
    }



    private fun loadChosenItineraryDetails(groupId: String) {
        firestore.collection("groupChats").document(groupId)
            .collection("chosenItineraries")
            .get() // This gets all documents in chosenItineraries, assuming there's only one.
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    // Get the first document, assuming there's only one chosen itinerary.
                    val chosenItinerary = documents.documents.first().toObject(ChatMessage::class.java)
                    chosenItinerary?.let {
                        // Update the TextViews with the itinerary details
                        findViewById<TextView>(R.id.itinerary_title).text = it.itineraryTitle
                        findViewById<TextView>(R.id.itinerary_description).text = it.itineraryDescription

                        // Show the Toast message
                        Toast.makeText(this, "Chosen itinerary: ${it.itineraryTitle}", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this, "No chosen itinerary available.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error loading chosen itinerary: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun showAddCommentDialog(messageId: String, itineraryId: String?) {
        val view = layoutInflater.inflate(R.layout.dialog_add_comment, null)
        val commentField = view.findViewById<EditText>(R.id.comment_field)
        val commitButton = view.findViewById<Button>(R.id.commit)
        val dialog = AlertDialog.Builder(this)
            .setTitle("Add Comment")
            .setMessage("Write your comment below:")
            .setView(view)
            .setNegativeButton("Cancel", null)
            .create()

        commitButton.setOnClickListener {
            val commentText = commentField.text.toString()
            // Now also pass groupId to the addComment function
            if (groupId != null) {
                addComment(messageId, commentText, itineraryId, groupId!!)
            } else {
                Toast.makeText(this, "Group ID is not available", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        dialog.show()
    }


    private fun addCommentToItinerary(comment: Comment, itineraryId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        // Use the user ID to reference the correct path for itineraries subcollection
        val itineraryCommentsRef = firestore.collection("users").document(userId)
            .collection("itineraries").document(itineraryId)
            .collection("comments")

        // Add the comment to the Itinerary's 'comments' subcollection
        itineraryCommentsRef.add(comment)
            .addOnSuccessListener {
                Log.d(TAG, "Comment added successfully to itinerary: $itineraryId")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error adding comment to itinerary $itineraryId", e)
            }
    }

    private fun addComment(messageId: String, commentText: String, itineraryId: String?, groupId: String) {
        val userId = firebaseAuth.currentUser?.uid
        if (userId != null) {
            // Retrieve the group name from Firestore
            firestore.collection("groupChats").document(groupId)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    val groupName = documentSnapshot.getString("name") ?: "Unnamed Group"

                    val comment = Comment(
                        userId = userId,
                        groupId = groupId,
                        groupName = groupName,
                        text = commentText
                    )

                    // Proceed to add the comment to the ChatMessage
                    val messageCommentRef = firestore.collection("groupChats").document(groupId)
                        .collection("chatMessages").document(messageId)

                    messageCommentRef.update("comments", FieldValue.arrayUnion(comment))
                        .addOnSuccessListener {
                            Log.d(TAG, "Comment added successfully to chat message")
                            if (!itineraryId.isNullOrEmpty()) {
                                // Then, add the comment to the itinerary
                                addCommentToItinerary(comment, itineraryId)
                            } else {
                                Log.e(TAG, "Itinerary ID is null or empty.")
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Error adding comment to chat message", e)
                        }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error fetching group chat name", e)
                }
        } else {
            Toast.makeText(this, "No user is logged in", Toast.LENGTH_SHORT).show()
        }
    }


    private fun selectImage() {
        val intent = Intent().apply {
            type = "image/*"
            action = Intent.ACTION_GET_CONTENT
        }
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            val filePath = data.data
            uploadImage(filePath)
        }
    }


    // Upload image to Firebase Storage
    fun uploadImage(filePath: Uri?) {
        if (filePath != null) {
            val groupId = intent.getStringExtra("GROUP_ID") ?: return
            val ref = FirebaseStorage.getInstance().getReference("/groupChats/$groupId/images/${UUID.randomUUID()}")

            ref.putFile(filePath)
                .addOnSuccessListener {
                    ref.downloadUrl.addOnSuccessListener {
                        saveImageInfo(it.toString())
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
                }
        }
    }

    fun saveImageInfo(url: String) {
        val groupId = intent.getStringExtra("GROUP_ID") ?: return
        val image = Image(url)
        FirebaseFirestore.getInstance().collection("groupChats").document(groupId)
            .collection("images").add(image)
            .addOnSuccessListener {
                Toast.makeText(this, "Image uploaded successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
            }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.groupchat_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Pass the menu item to the drawer toggle to see if it can handle the click
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }

        // Handle other menu items here
        return when (item.itemId) {
            R.id.home -> {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.back -> {
                val intent = Intent(this, GroupChatActivity::class.java)
                intent.putExtra("itinerary", itinerary)
                startActivity(intent)
                true
            }
            R.id.maps -> {
                val intent = Intent(this, MapsViewActivity::class.java)
                val groupId = this.intent.getStringExtra("GROUP_ID")
                if (groupId != null) {
                    intent.putExtra("GROUP_ID", groupId)
                }
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}