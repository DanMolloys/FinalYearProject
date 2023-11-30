package com.example.finalyearprojectdm

import java.time.LocalDate
import java.util.Date

data class Holiday(

    var startingLocation: String = "",
    var startDate: LocalDate? = null,
    var budget: Double = 0.0,
    var amountOfPersons: Int = 0,
    var thingsToDo: ArrayList<String> = arrayListOf<String>()
) {
    companion object {
        lateinit var startingLocation: String
        var startDate: LocalDate? = null
        var budget: Double = 0.0
        var amountOfPersons: Int = 0
        var thingsToDo: ArrayList<String> = arrayListOf<String>()
    }
}
