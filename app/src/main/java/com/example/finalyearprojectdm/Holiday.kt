package com.example.finalyearprojectdm

import java.util.Date

data class Holiday(
    var startingLocation: String = "",
    var startDate: Date? = null,
    var budget: Double = 0.0,
    val amountOfPersons: Int = 0,
    val thingsToDo: ArrayList<String> = arrayListOf<String>()
)
