package com.example.finalyearprojectdm

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.finalyearprojectdm.databinding.ActivityMainBinding
import com.example.finalyearprojectdm.databinding.ActivitySigninBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding:ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

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
}