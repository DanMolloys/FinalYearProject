package com.example.finalyearprojectdm

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.Serializable

class ChatActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private var chatMessages = mutableListOf<ChatMessage>()

    private lateinit var toolbar: Toolbar

    private var itinerary: Itinerary? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        recyclerView = findViewById(R.id.chat_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""

        // Initialize chatAdapter here
        chatAdapter = ChatAdapter(chatMessages, FirebaseAuth.getInstance().currentUser?.uid ?: "", onProposalClick = { chatMessage ->
            // Create a new BottomSheetDialog
            val bottomSheetDialog = BottomSheetDialog(this)
            val view = layoutInflater.inflate(R.layout.bottom_sheet_proposal, null)

            // Set the itinerary details in the bottom sheet
            val itinerary = chatMessage.itinerary
            if (itinerary != null) {
                view.findViewById<TextView>(R.id.bottom_dialog_title_text_view).text = itinerary.title
                view.findViewById<TextView>(R.id.bottom_dialog_description_text_view).text = itinerary.description
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
    }


    //loading chat messages from Firestore.
    private fun loadChatMessages() {
        val groupId = intent.getStringExtra("GROUP_ID") ?: return
        FirebaseFirestore.getInstance().collection("groupChats").document(groupId)
            .collection("chatMessages")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val message = document.toObject(ChatMessage::class.java)
                    chatMessages.add(message)
                }
                chatAdapter.notifyDataSetChanged()
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