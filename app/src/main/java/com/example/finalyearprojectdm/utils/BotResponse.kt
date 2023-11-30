package com.example.finalyearprojectdm.utils

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.finalyearprojectdm.Holiday
import com.example.finalyearprojectdm.Itinerary
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern
import java.util.*


//Add


object BotResponse {
    val holiday = Holiday()
    val itinerary = Itinerary()


    @RequiresApi(Build.VERSION_CODES.O)
    fun basicResponces(message: String): Any {

        val random = (0..2).random()
        val mes = message.toLowerCase()
        var counter: Int = 0

        val userInputIsCountry = isCountry(message)


        var plop: String = ""
        plop = "Madrid"

        // Regex pattern to identify a date in the format dd/MM/yyyy
        val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val datePattern = Pattern.compile("\\b(\\d{2}/\\d{2}/\\d{4})\\b")
        val dateMatcher = datePattern.matcher(message)

        // Regex pattern to identify a double in the message
        val doublePattern = Pattern.compile("\\b(\\d+\\.\\d+)\\b")
        val doubleMatcher = doublePattern.matcher(message)

        // Regex pattern to identify an integer in the message
        val intPattern = Pattern.compile("\\b(\\d+)\\b")
        val intMatcher = intPattern.matcher(message)

        val predefinedPhrases = listOf("fishing", "eating", "swimming")


        //each response adds to counter
        counter++

        return when {

            //Start off, if message contains location
            userInputIsCountry -> {
                // Set value
                Holiday.startingLocation = message
                "Great choice! So we're thinking " + Holiday.startingLocation + ". When would you like to go!"
            }

            dateMatcher.find() -> {
                val foundDate = dateMatcher.group(1)
                // Parse the found date into a LocalDate object
                val date = LocalDate.parse(foundDate, dateFormatter)
                Holiday.startDate = date
                "Okay so on the " + Holiday.startDate + ". What is your budget for the trip?"
            }

            doubleMatcher.find() -> {
                val foundDouble = doubleMatcher.group(1).toDouble()
                // Set the budget in the Holiday class
                Holiday.budget = foundDouble
                "With a budget of " + Holiday.budget + " perfect. Let's plan your trip accordingly! How many of yous are going?"
            }

            intMatcher.find() -> {
                val foundInt = intMatcher.group(1).toInt()
                // Set the number of persons in the Holiday class
                Holiday.amountOfPersons = foundInt
                "With " + Holiday.amountOfPersons + " persons. Let's plan an amazing trip for everyone!, What does everyone like doing?"
            }

            predefinedPhrases.any { message.toLowerCase().contains(it, ignoreCase = true) } -> {
                val foundPhrase = predefinedPhrases.first { message.contains(it, ignoreCase = true) }
                // Add the found string to the thingsToDo ArrayList in the Holiday class
                Holiday.thingsToDo.add(foundPhrase)
                "Okay so \"$foundPhrase\" is on the agenda. Is there anything else? If not just type 'NOPE'"
            }

            message.contains("NOPE", ignoreCase = true) -> {
                val thingsToDoStr = StringBuilder()
                for (thing in Holiday.thingsToDo) {
                    thingsToDoStr.append(thing).append(", ")
                }
                    // Set value
                        return "Okay so your holiday plan so far is.... \n " +
                                "Location: " + Holiday.startingLocation + "\n " +
                                "Date: " + Holiday.startDate + "\n " +
                                "Budget: " + Holiday.budget + "\n " +
                                "Companions: " + Holiday.amountOfPersons + "\n " +
                                "Doing: " + thingsToDoStr.toString()
                }
            else -> {
                "I'm not sure how to respond to that. Can you provide more details?"
            }
            }

        }

    //using a java plugin that contains locations
    //appears to only have coutries in the plugin, may use an API intead.
    fun isCountry(input: String): Boolean {
        val availableLocales: Array<Locale> = Locale.getAvailableLocales()

        for (locale in availableLocales) {
            if (locale.displayCountry.equals(input, ignoreCase = true)) {
                return true
            }
        }
        return false
    }
    }
