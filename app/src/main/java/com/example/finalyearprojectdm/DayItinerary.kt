package com.example.finalyearprojectdm

import java.io.Serializable

data class DayItinerary(
    var dayNumber: Int = 0,
    var dayDescription: String = ""
): Serializable {
    lateinit var description: String
}
