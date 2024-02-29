package com.example.finalyearprojectdm

data class DayItinerary(
    var dayNumber: Int = 0,
    var dayDescription: String = ""
) {
    lateinit var description: String
}
