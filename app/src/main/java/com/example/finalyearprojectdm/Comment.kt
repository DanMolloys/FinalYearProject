package com.example.finalyearprojectdm

import java.io.Serializable

data class Comment(
    var userId: String = "",
    var groupId: String = "",
    var groupName: String = "",
    var text: String = ""
) : Serializable

