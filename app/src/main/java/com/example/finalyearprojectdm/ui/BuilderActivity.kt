package com.example.finalyearprojectdm.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.finalyearprojectdm.Holiday
import com.example.finalyearprojectdm.Itinerary
import com.example.finalyearprojectdm.MainActivity
import com.example.finalyearprojectdm.R
import com.example.finalyearprojectdm.SingInActivity
import com.example.finalyearprojectdm.data.Message
import com.example.finalyearprojectdm.databinding.ActivityBuilderBinding
import com.example.finalyearprojectdm.utils.BotResponse
import com.example.finalyearprojectdm.utils.BotResponse.holiday
import com.example.finalyearprojectdm.utils.Constants.RECEIVE_ID
import com.example.finalyearprojectdm.utils.Constants.SEND_ID
import com.example.finalyearprojectdm.utils.Time
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class BuilderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBuilderBinding
    private lateinit var adapter: MessageAdapter

    //http kept timing out, increasing time limit
    private val client = OkHttpClient.Builder()
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private lateinit var firestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var toolbar: Toolbar



    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBuilderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""

        binding.apply {
            recyclerView()
            clickEvents()
            customMessage("Hello! My name is Globe, are you ready to build a holiday with me? \nWhere would you like to go")
        }

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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun clickEvents() {
        binding.buttonSend.setOnClickListener {
            sendMessage()
        }

        binding.etMessage.setOnClickListener {
            GlobalScope.launch {
                withContext(Dispatchers.Main) {
                    binding.rvMessages.scrollToPosition(adapter.itemCount - 1)
                }
            }
        }

        binding.buttonGenerate.setOnClickListener {
            val message = binding.etMessage.text.toString()
            genItinerary(message)
        }
    }

    private fun recyclerView() {
        adapter = MessageAdapter()
        binding.rvMessages.adapter = adapter
        binding.rvMessages.layoutManager = LinearLayoutManager(applicationContext)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendMessage() {
        val message = binding.etMessage.text.toString()
        val timeStamp = Time.timeStamp()

        if (message.isNotEmpty()) {
            binding.etMessage.setText("")
            adapter.addMessage(Message(message, SEND_ID, timeStamp))

            //scroll to bottom all time
            binding.rvMessages.scrollToPosition(adapter.itemCount - 1)

            //take message and give to bot
            botResponse(message)
        }
    }

    private fun genItinerary(message: String) {
        val apiKey = ""

        val thingsToDo = Holiday.thingsToDo.joinToString(", ")

        //message to be sent to API
        val message = "Create a day by day holiday itinerary in " + Holiday.startingLocation +
                " starting on the " + Holiday.startDate +
                " with " + Holiday.amountOfPersons + " amount of people" +
                " with a budget of " + Holiday.budget +
                " and include " + thingsToDo + " while giving specific locations and things to do with " +
                "estimate prices after each day and a total expected price at the end"
        val prompt = message

        val JSON = "application/json; charset=utf-8".toMediaTypeOrNull()

        val body = RequestBody.create(
            JSON, """
        {
            "prompt": "$prompt",
            "max_tokens": 500
        }
    """.trimIndent()
        )

        val request = Request.Builder()
            //free version is /text-davinci-002/
            .url("https://api.openai.com/v1/engines/gpt-3.5-turbo-instruct/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(body)
            .build()

        GlobalScope.launch(Dispatchers.IO) {
            val response = client.newCall(request).execute()
            val result = response.body?.string()

            withContext(Dispatchers.Main) {
                // Display the result here.
                val jsonObject = JSONObject(result)
                val text = jsonObject.getJSONArray("choices").getJSONObject(0).getString("text")
                customMessage(text ?: "No Result")

                val itinerary = Itinerary()
                itinerary.description = text
                itinerary.title = Holiday.startingLocation + " trip for " + Holiday.amountOfPersons

                addItineraryToFirestore(itinerary)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun botResponse(message: String) {

        GlobalScope.launch {
            withContext(Dispatchers.Main) {
                val response = BotResponse.basicResponces(message)
                val timeStamp = Time.timeStamp()

                adapter.addMessage((Message(response, RECEIVE_ID, timeStamp)))
                binding.rvMessages.scrollToPosition(adapter.itemCount - 1)

            }
        }

    }

    private fun customMessage(message: String) {
        GlobalScope.launch {
            withContext(Dispatchers.Main) {
                val timeStamp = Time.timeStamp()
                adapter.addMessage(Message(message, RECEIVE_ID, timeStamp))
                binding.rvMessages.scrollToPosition(adapter.itemCount - 1)

            }
        }
    }

    private fun addItineraryToFirestore(itinerary: Itinerary) {
        val user = firebaseAuth.currentUser
        if (user != null) {
            val itineraryMap = hashMapOf(
                "title" to itinerary.title,
                "description" to itinerary.description,
                "startingLocation" to Holiday.startingLocation,
                "startDate" to Holiday.startDate.toString(),
                "budget" to Holiday.budget.toString(),
                "amountOfPersons" to Holiday.amountOfPersons.toString(),
                "thingsToDo" to Holiday.thingsToDo.toString()
            )

            firestore.collection("users").document(user.uid).collection("itineraries")
                .add(itineraryMap)
                .addOnSuccessListener {
                    Toast.makeText(this, "Itinerary added successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    customMessage("Error adding itinerary: ${e.message}")
                }
        } else {
            Toast.makeText(this, "No user is logged in", Toast.LENGTH_SHORT).show()
        }
    }


}