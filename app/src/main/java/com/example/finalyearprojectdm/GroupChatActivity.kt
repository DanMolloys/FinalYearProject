package com.example.finalyearprojectdm

import GroupChatAdapter
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class GroupChatActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var groupChatAdapter: GroupChatAdapter
    private val groupChats = mutableListOf<GroupChat>()

    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_groupchat)

        recyclerView = findViewById(R.id.group_chat_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""

        groupChatAdapter = GroupChatAdapter(groupChats) { groupChat ->
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("GROUP_ID", groupChat.id) // pass the group chat ID to the chat activity
            startActivity(intent)
        }
        recyclerView.adapter = groupChatAdapter

        findViewById<FloatingActionButton>(R.id.new_group_chat_button).setOnClickListener {
            val intent = Intent(this, NewGroupChatActivity::class.java)
            startActivity(intent)
        }

        loadGroupChats()
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

    private fun loadGroupChats() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance().collection("groupChats")
            .whereArrayContains("userIds", userId)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    // Handle error here
                    return@addSnapshotListener
                }

                groupChats.clear()

                for (document in snapshots!!) {
                    val groupChat = document.toObject(GroupChat::class.java).copy(id = document.id)
                    groupChats.add(groupChat)
                }

                groupChatAdapter.notifyDataSetChanged()
            }
    }
}