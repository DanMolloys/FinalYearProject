package com.example.finalyearprojectdm

data class ApiResponse(
    val status: Boolean,
    val message: String,
    val timestamp: Long,
    val data: FlightData
)
