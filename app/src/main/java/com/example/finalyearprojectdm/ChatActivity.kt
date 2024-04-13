package com.example.finalyearprojectdm

import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.Serializable
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        recyclerView = findViewById(R.id.chat_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""



        // Initialize chatAdapter here
        chatAdapter = ChatAdapter(chatMessages, FirebaseAuth.getInstance().currentUser?.uid ?: "", onProposalClick = { chatMessage ->
            // Create a new BottomSheetDialog
            val bottomSheetDialog = BottomSheetDialog(this)
            val view = layoutInflater.inflate(R.layout.bottom_sheet_proposal, null)

            // Set the itinerary details in the bottom sheet


            // Add click listeners for the voting buttons
            view.findViewById<Button>(R.id.vote_green_button).setOnClickListener {
                castVote(chatMessage.id, "green")
                bottomSheetDialog.dismiss()
            }
            view.findViewById<Button>(R.id.vote_red_button).setOnClickListener {
                castVote(chatMessage.id, "red")
                bottomSheetDialog.dismiss()
            }

            view.findViewById<Button>(R.id.add_comment_button).setOnClickListener {
                showAddCommentDialog(chatMessage.id)
                bottomSheetDialog.dismiss()
            }

            bottomSheetDialog.setContentView(view)
            bottomSheetDialog.show()
        })

        recyclerView.adapter = chatAdapter

        loadChatMessages()

        findViewById<Button>(R.id.send_message_button).setOnClickListener {
            val messageText = findViewById<EditText>(R.id.message_field).text.toString()

            val message =
                ChatMessage(
                    text = messageText,
                    senderId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                )

            // Get a reference to the current group chat's messages subcollection
            val groupId = intent.getStringExtra("GROUP_ID") ?: return@setOnClickListener
            FirebaseFirestore.getInstance().collection("groupChats").document(groupId)
                .collection("chatMessages").add(message)
                .addOnSuccessListener {
                    chatMessages.add(message)
                    chatAdapter.notifyDataSetChanged()
                    findViewById<EditText>(R.id.message_field).text.clear()

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
    }


    //loading chat messages from Firestore.
    //using snapshot so when vote is added, the UI will update automatically.
    private fun loadChatMessages() {
        val groupId = intent.getStringExtra("GROUP_ID") ?: return
        FirebaseFirestore.getInstance().collection("groupChats").document(groupId)
            .collection("chatMessages")
            .orderBy("timestamp") // Optional: sort messages by timestamp
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    Log.w(TAG, "Listen failed.", firebaseFirestoreException)
                    return@addSnapshotListener
                }

                if (querySnapshot != null) {
                    chatMessages.clear() // Clear the old list
                    for (document in querySnapshot.documents) {
                        val message = document.toObject(ChatMessage::class.java)
                        if (message != null) {
                            message.id = document.id // Save the document ID in the message
                            chatMessages.add(message)
                        }
                    }
                    chatAdapter.notifyDataSetChanged()
                }
            }
    }

    //send an itinerary proposal as a chat message.
    private fun sendItineraryProposal(itinerary: Itinerary) {
        val message = ChatMessage(
            text = itinerary.title, // use the itinerary title as the message text
            senderId = FirebaseAuth.getInstance().currentUser?.uid ?: "",
            itinerary = itinerary
        )

        // Get the group ID from the intent. If it's null, return immediately.
        val groupId = intent.getStringExtra("GROUP_ID") ?: return
        // Add the message to the Firestore collection
        FirebaseFirestore.getInstance().collection("groupChats").document(groupId)
            .collection("chatMessages").add(message)
            .addOnSuccessListener {
                chatMessages.add(message)
                chatAdapter.notifyDataSetChanged()
            }
    }


    //maybe use Firebase Transactions?
    private fun castVote(messageId: String, vote: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val groupId = intent.getStringExtra("GROUP_ID") ?: return
        val messageRef = FirebaseFirestore.getInstance().collection("groupChats").document(groupId)
            .collection("chatMessages").document(messageId)

        messageRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val votes = document.get("votes") as? MutableMap<String, String> ?: mutableMapOf()
                    votes[userId] = vote
                    messageRef.update("votes", votes)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Vote recorded successfully!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Error recording vote", e)
                            Toast.makeText(this, "Failed to record vote", Toast.LENGTH_SHORT).show()
                        }
                }
            }
    }

    private fun showAddCommentDialog(messageId: String) {
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
            addComment(messageId, commentText)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun addComment(messageId: String, commentText: String) {
        val user = firebaseAuth.currentUser
        if (user != null) {
            val groupId = intent.getStringExtra("GROUP_ID") ?: return

            // Create a Comment object
            val comment = Comment(
                userId = user.uid,
                userName = user.displayName ?: "", // replace with actual user name
                groupId = groupId,
                groupName = "", // replace with actual group name
                text = commentText
            )

            // Add comment to the chat message
            val messageCommentRef = FirebaseFirestore.getInstance().collection("groupChats").document(groupId)
                .collection("chatMessages").document(messageId)

            messageCommentRef.update("comments", FieldValue.arrayUnion(comment))
                .addOnSuccessListener {
                    Toast.makeText(this, "Comment added successfully to chat message", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error adding comment to chat message", e)
                    Toast.makeText(this, "Failed to add comment to chat message", Toast.LENGTH_SHORT).show()
                }

            // Add comment to the original Itinerary, still not fully working
            val itineraryRef = FirebaseFirestore.getInstance().collection("users").document(user.uid)
                .collection("itineraries").document(itinerary?.id ?: return)

            itineraryRef.update("comments", FieldValue.arrayUnion(comment))
                .addOnSuccessListener {
                    Toast.makeText(this, "Comment added successfully to itinerary", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Error adding comment to itinerary", e)
                    Toast.makeText(this, "Failed to add comment to itinerary", Toast.LENGTH_SHORT).show()
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
        return when (item.itemId) {
            R.id.home -> {
                val intent = Intent (this, MainActivity ::class.java)
                startActivity(intent)
                true
            }
            R.id.back -> {
                val intent = Intent(this, GroupChatActivity::class.java)
                // Pass the itinerary back to GroupChatActivity
                intent.putExtra("itinerary", itinerary)
                startActivity(intent)
                true
            }
            R.id.maps -> {
                // Create a new intent for MapsViewActivity
                val intent = Intent(this, MapsViewActivity::class.java)

                // Get the group ID from the current activity's intent
                val groupId = this.intent.getStringExtra("GROUP_ID")
                if (groupId != null) {
                    // Put the GROUP_ID into the new intent
                    intent.putExtra("GROUP_ID", groupId)
                }

                // Start the MapsViewActivity
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}