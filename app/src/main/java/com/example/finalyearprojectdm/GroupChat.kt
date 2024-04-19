package com.example.finalyearprojectdm

data class GroupChat(
    var id: String = "",
    val name: String = "",
    val userIds: List<String> = listOf(),
    val creator: String = ""
)