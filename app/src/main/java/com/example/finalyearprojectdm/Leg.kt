package com.example.finalyearprojectdm

data class Leg(
    val originStationCode: String,
    val destinationStationCode: String,
    val departureDateTime: String,
    val arrivalDateTime: String,
    val classOfService: String,
    val marketingCarrierCode: String,
    val operatingCarrierCode: String,
    val flightNumber: Int,
    val numStops: Int,
    val distanceInKM: Double,
    val isInternational: Boolean,
    val operatingCarrier: Carrier,
    val marketingCarrier: Carrier
)
