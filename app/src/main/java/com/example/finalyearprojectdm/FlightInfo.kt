package com.example.finalyearprojectdm

import com.google.gson.JsonElement

data class FlightInfo(
    val status: Boolean,
    val message: JsonElement,
    val timestamp: Long,
    val data: Data
)