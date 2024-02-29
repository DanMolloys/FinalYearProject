package com.example.finalyearprojectdm

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ChatActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private val chatMessages = mutableListOf<ChatMessage>()

    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        recyclerView = findViewById(R.id.chat_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        chatAdapter = ChatAdapter(chatMessages, currentUserId)
        recyclerView.adapter = chatAdapter

        loadChatMessages()

        findViewById<Button>(R.id.send_message_button).setOnClickListener {
            val messageText = findViewById<EditText>(R.id.message_field).text.toString()

            val message = ChatMessage(
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
    }

    private fun loadChatMessages() {
        val groupId = intent.getStringExtra("GROUP_ID") ?: return
        // Read from the group chat's messages subcollection
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
                val intent = Intent (this, GroupChatActivity ::class.java)
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