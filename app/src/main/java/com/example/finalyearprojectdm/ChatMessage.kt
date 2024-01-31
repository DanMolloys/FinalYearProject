package com.example.finalyearprojectdm

import java.util.Date

data class ChatMessage(
    val id: String = "",
    val text: String = "",
    val senderId: String = "",
    val timestamp: Date = Date()
)