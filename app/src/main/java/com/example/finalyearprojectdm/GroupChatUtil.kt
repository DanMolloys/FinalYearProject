package com.example.finalyearprojectdm

import com.google.firebase.firestore.FirebaseFirestore

object GroupChatUtil {
    fun getGroupUsers(groupId: String, onSuccess: (List<String>) -> Unit) {
        FirebaseFirestore.getInstance().collection("groupChats")
            .document(groupId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val groupChat = documentSnapshot.toObject(GroupChat::class.java)
                if (groupChat != null) {
                    println("User IDs in group chat: ${groupChat.userIds}")  // print the user IDs
                    onSuccess(groupChat.userIds)
                } else {
                    println("Failed to convert document to GroupChat object. Document data: ${documentSnapshot.data}")
                }
            }
            .addOnFailureListener { exception ->
                println("Error getting document: $exception")
            }
    }
}