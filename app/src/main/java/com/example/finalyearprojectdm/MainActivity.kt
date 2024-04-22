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
import android.os.Handler
import android.os.Looper
import androidx.viewpager2.widget.ViewPager2
import me.relex.circleindicator.CircleIndicator3
import java.util.Timer
import java.util.TimerTask

class MainActivity : AppCompatActivity() {

    private lateinit var binding:ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var toolbar: Toolbar

    private lateinit var viewPager: ViewPager2
    private val handler = Handler(Looper.getMainLooper())
    private var timer: Timer? = null


    private val PERMISSION_REQUEST_CODE = 1234

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firebaseAuth = FirebaseAuth.getInstance()

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""

        viewPager = findViewById(R.id.viewPager)
        viewPager.adapter = ScreenSlidePagerAdapter(this)
        setupAutoScroll()

        val indicator: CircleIndicator3 = findViewById(R.id.indicator)
        indicator.setViewPager(viewPager)

        if (checkPermissions()) {
            val intent = Intent(this, LocationService::class.java)
            startService(intent)
        } else {
            requestPermissions()
        }
    }
    
    private fun setupAutoScroll() {
        val runnable = Runnable {
            val currentItem = viewPager.currentItem
            viewPager.currentItem = (currentItem + 1) % 3 // There are 3 pages
        }
        timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                handler.post(runnable)
            }
        }, 15000, 15000) // delay, period
    }

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
                // permission denied
            }
            return
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