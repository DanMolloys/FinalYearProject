package com.example.finalyearprojectdm

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class NewGroupChatActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var userSelectionAdapter: UserSelectionAdapter
    private val users = mutableListOf<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_groupchat)

        recyclerView = findViewById(R.id.user_selection_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        userSelectionAdapter = UserSelectionAdapter(users)
        recyclerView.adapter = userSelectionAdapter

        loadUsers()

        findViewById<Button>(R.id.create_group_chat_button).setOnClickListener {
            val db = FirebaseFirestore.getInstance()
            val groupChatRef = db.collection("groupChats").document()

            val groupName = findViewById<EditText>(R.id.group_chat_name_field).text.toString()
            val selectedUserIds = userSelectionAdapter.getSelectedUserIds().toMutableList()
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            selectedUserIds.add(currentUserId)

            val groupChat = GroupChat(id = groupChatRef.id, name = groupName, userIds = selectedUserIds, creator = currentUserId)
            groupChatRef.set(groupChat)
                .addOnSuccessListener {
                    Toast.makeText(this, "Group Chat Created", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error Creating Group Chat", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun loadUsers() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        FirebaseFirestore.getInstance().collection("users")
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val id = document.id // get id from the document reference
                    if (id == currentUserId) continue // if this user is the current user, skip this iteration
                    val email = document.getString("email") ?: "" // get email from the document
                    val user = User(id, email)
                    users.add(user)
                }
                userSelectionAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error Loading Users: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}