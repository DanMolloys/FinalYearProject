package com.example.finalyearprojectdm

import java.io.Serializable
import java.util.Date

data class ChatMessage(
    var id: String = "",
    val text: String = "",
    val senderId: String = "",
    val timestamp: Date = Date(),
    val itinerary: Itinerary? = null,
    var votes: MutableMap<String, String> = mutableMapOf(),
    var comments: MutableList<Comment> = mutableListOf()
) : Serializable