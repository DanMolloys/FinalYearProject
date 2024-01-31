package com.example.finalyearprojectdm

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class ChatAdapter(
    private val chatMessages: List<ChatMessage>,
    private val currentUserId: String
) : RecyclerView.Adapter<ChatAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_message, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val isCurrentUser = currentUserId == chatMessages[position].senderId
        holder.bind(chatMessages[position], isCurrentUser)
    }

    override fun getItemCount() = chatMessages.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(R.id.message_text)
        private val messageContainer: LinearLayout = itemView.findViewById(R.id.message_container)

        fun bind(message: ChatMessage, isCurrentUser: Boolean) {
            textView.text = message.text
            val layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
            if (isCurrentUser) {
                // if current user's message, shift to the right-hand side
                layoutParams.gravity = Gravity.END
            } else {
                layoutParams.gravity = Gravity.START
            }
            messageContainer.layoutParams = layoutParams
        }
    }
}