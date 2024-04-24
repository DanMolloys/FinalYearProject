package com.example.finalyearprojectdm

import java.io.Serializable
import java.util.Date

data class ChatMessage(
    var id: String = "",
    val text: String = "",
    val senderId: String = "",
    var imageResourceId: Int = R.drawable.baseline_add_24,
    val timestamp: Date = Date(),
    val itineraryTitle: String? = null,
    val itineraryDescription: String? = null,
    val itineraryId: String? = null,
    var votes: MutableMap<String, String> = mutableMapOf(),
    var comments: MutableList<Comment> = mutableListOf()
) : Serializable
