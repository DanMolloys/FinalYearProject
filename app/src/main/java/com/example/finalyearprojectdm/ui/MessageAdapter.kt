package com.example.finalyearprojectdm.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.finalyearprojectdm.R
import com.example.finalyearprojectdm.data.Message
import com.example.finalyearprojectdm.databinding.ActivityStoredproposalsBinding
import com.example.finalyearprojectdm.databinding.MessageItemBinding
import com.example.finalyearprojectdm.utils.Constants.RECEIVE_ID
import com.example.finalyearprojectdm.utils.Constants.SEND_ID



class MessageAdapter : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    var messageList = mutableListOf<Message>()

    inner class MessageViewHolder(val binding: MessageItemBinding) : RecyclerView.ViewHolder(binding.root) {

        //remove message in chat function
        init {
            itemView.setOnClickListener {
                //removal
                messageList.removeAt(adapterPosition)
                //alret of removal
                notifyItemRemoved(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {

        //add message item to inflated layout

        val binding =
            MessageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MessageViewHolder(binding)
    }


    override fun getItemCount(): Int {
        //amount of messages in chat
        return messageList.size
    }

    //diff between you sending and bot sending
    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val currentMessage = messageList[position]

        when (currentMessage.id) {
            //user sending a message, making users visible and bots gone
            //plugin used "kotlin-android-extensions"
            SEND_ID -> {
                holder.binding.tvMessage.apply {
                    text = currentMessage.message
                    visibility = View.VISIBLE
                }
                holder.binding.tvBotMessage.visibility = View.GONE
            }

            //same as above but for bots messages
            RECEIVE_ID -> {
                holder.binding.tvBotMessage.apply {
                    text = currentMessage.message
                    visibility = View.VISIBLE
                }
                holder.binding.tvMessage.visibility = View.GONE
            }
        }
    }

    fun addMessage(message: Message) {
        //add the message to the chat
        this.messageList.add(message)
        notifyItemInserted(messageList.size)
    }
}