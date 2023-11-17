package com.example.finalyearprojectdm.utils

import java.sql.Date
import java.sql.Timestamp
import java.text.SimpleDateFormat

object Time {

    //function to get the time
    //made here casue idk where else i might use the function everywhere
    fun timeStamp() : String {
        val timeStamp = Timestamp(System.currentTimeMillis())
        val sdf = SimpleDateFormat("HH:mm")
        val time = sdf.format(Date(timeStamp.time))

        return time.toString()
    }
}