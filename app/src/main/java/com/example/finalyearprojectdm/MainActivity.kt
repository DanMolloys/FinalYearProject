package com.example.finalyearprojectdm

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import com.example.finalyearprojectdm.databinding.ActivityMainBinding
import com.example.finalyearprojectdm.ui.BuilderActivity
import com.google.firebase.auth.FirebaseAuth
import android.Manifest

class MainActivity : AppCompatActivity() {

    private lateinit var binding:ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var toolbar: Toolbar


    private val PERMISSION_REQUEST_CODE = 1234

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true
        }
        return false
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION),
            PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // permissions were granted, start the location service
                val intent = Intent(this, LocationService::class.java)
                startService(intent)
            } else {
                // permissions were denied
            }
            return
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""

        // Start the LocationService
        if (checkPermissions()) {
            // permissions already granted, start the location service
            val intent = Intent(this, LocationService::class.java)
            startService(intent)
        } else {
            // permissions not granted, request them
            requestPermissions()
        }



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
                val intent = Intent (this, ProfileUpdateActivity ::class.java)
                startActivity(intent)
                true
            }
            R.id.logOut -> {
                firebaseAuth.signOut()
                // Stop the LocationService
                val intent = Intent(this, LocationService::class.java)
                stopService(intent)
                // Redirect to login page
                val loginIntent = Intent (this, SingInActivity ::class.java)
                startActivity(loginIntent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}