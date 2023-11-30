package com.example.finalyearprojectdm.ui

import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.finalyearprojectdm.data.Message
import com.example.finalyearprojectdm.databinding.ActivityBuilderBinding
import com.example.finalyearprojectdm.utils.BotResponse
import com.example.finalyearprojectdm.utils.Constants.RECEIVE_ID
import com.example.finalyearprojectdm.utils.Constants.SEND_ID
import com.example.finalyearprojectdm.utils.Time
import com.google.firebase.auth.FirebaseAuth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BuilderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBuilderBinding
    private lateinit var adapter: MessageAdapter


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