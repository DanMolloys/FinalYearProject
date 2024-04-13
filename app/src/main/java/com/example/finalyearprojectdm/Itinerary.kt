package com.example.finalyearprojectdm

import java.io.Serializable

data class Itinerary(
    var id: String = "",
    var userId: String = "",

    var airportCode: String = "",
    var title: String = "",
    var description: String = "",
    var startingLocation: String = "",
    var startDate: String = "",
    var budget: String = "",
    var amountOfPersons: String = "",
    var thingsToDo: String = "",

    var days: MutableList<DayItinerary> = mutableListOf(),
    val comments: MutableList<Comment> = mutableListOf()
) : Serializable