package com.example.finalyearprojectdm

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import com.example.finalyearprojectdm.databinding.ActivityMainBinding
import com.example.finalyearprojectdm.ui.BuilderActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding:ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""


        binding.buttonChatBuilder.setOnClickListener {
            val intent = Intent(this, BuilderActivity :: class.java)
            startActivity(intent)
        }

        binding.buttonCurrentPro.setOnClickListener {
            val intent = Intent(this, StoredProposalsActivity :: class.java)
            startActivity(intent)
        }

        binding.buttonGroupChat.setOnClickListener {
            val intent = Intent(this, GroupChatActivity :: class.java)
            startActivity(intent)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.profile -> {
                // go to edit profile page
                true
            }
            R.id.logOut -> {
                firebaseAuth.signOut()
                val intent = Intent (this, SingInActivity ::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}