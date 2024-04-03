package com.example.finalyearprojectdm

import java.io.Serializable
import java.util.Date

data class ChatMessage(
    val id: String = "",
    val text: String = "",
    val senderId: String = "",
    val timestamp: Date = Date(),
    val itinerary: Itinerary? = null
) : Serializable