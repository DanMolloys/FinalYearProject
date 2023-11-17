package com.example.finalyearprojectdm.utils


object BotResponse {

    fun basicResponces(message: String):String {

        val random = (0..2).random()
        val mes = message.toLowerCase()

        return when {

            //Hello responses
            message.contains("hello") -> {
                when (random) {
                    0 -> "Hello"
                    1 -> "How are you"
                    2 -> "Wassup"

                    else -> "error"
                }
            }

            message.contains("how are you") -> {
                when (random) {
                    0 -> "Good, and you?"
                    1 -> "How are you????"
                    2 -> "Grand"

                    else -> "error"
                }
            }


            //beginning of building
            message.contains("Yes") -> {
                when (random) {
                    0 -> "Where do you plan on visiting?"
                    1 -> "Where would you like to being your trip?"
                    2 -> "Any locations in mind?"

                    else -> "error"
                }
            }


            message.contains("Yes") -> {
                when (random) {
                    0 -> "Where do you plan on visiting?"
                    1 -> "Where would you like to being your trip?"
                    2 -> "Any locations in mind?"

                    else -> "error"
                }
            }




            //coin flip if user cant make coice
            //message must contain flip and coin to run
            message.contains("flip") && message.contains("coin") -> ({
                var ranNum = (0..1).random()
                val result = if (ranNum == 0) "heads" else "tails"
            }).toString()

            //anything else responses
            else -> {
                when (random) {
                    0 -> "Sorry???"
                    1 -> "I dont get it"
                    2 -> "dunno"

                    else -> "error"
                }
            }
        }
    }
}