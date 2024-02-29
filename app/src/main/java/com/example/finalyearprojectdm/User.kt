package com.example.finalyearprojectdm

data class User(
    val id: String = "",
    val email: String = "",
    val imageResourceId: Long? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)