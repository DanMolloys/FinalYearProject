package com.example.finalyearprojectdm

import java.io.Serializable

data class Comment(
    var id: String = "",
    var userId: String = "",
    var userName: String = "",
    var groupId: String = "",
    var groupName: String = "",
    var text: String = ""
) : Serializable
