package com.example.finalyearprojectdm

data class FlightData(
    val session: Session,
    val complete: Boolean,
    val numOfFilters: Int,
    val totalNumResults: Int,
    val flights: List<Flight>
)
