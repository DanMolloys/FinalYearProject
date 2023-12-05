package com.example.finalyearprojectdm.ui

import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.finalyearprojectdm.Holiday
import com.example.finalyearprojectdm.Itinerary
import com.example.finalyearprojectdm.data.Message
import com.example.finalyearprojectdm.databinding.ActivityBuilderBinding
import com.example.finalyearprojectdm.utils.BotResponse
import com.example.finalyearprojectdm.utils.Constants.RECEIVE_ID
import com.example.finalyearprojectdm.utils.Constants.SEND_ID
import com.example.finalyearprojectdm.utils.Time

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

class BuilderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBuilderBinding
    private lateinit var adapter: MessageAdapter
    private val client = OkHttpClient()


    //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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBuilderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            recyclerView()
            clickEvents()
            customMessage("Hello! My name is Globe, are you ready to build a holiday with me? \nWhere would you like to go")
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
        val apiKey = "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"

        val thingsToDo = Holiday.thingsToDo.joinToString(", ")

        //message to be sent to API
        val message = "Create a day by day holiday itinerary in " + Holiday.startingLocation +
                " on the " + Holiday.startDate +
                " with " + Holiday.amountOfPersons + " amount of people" +
                " with a budget of " + Holiday.budget +
                " and include " + thingsToDo
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
            .url("https://api.openai.com/v1/engines/text-davinci-001/completions")
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


}