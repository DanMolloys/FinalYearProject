package com.example.finalyearprojectdm

import android.content.ContentValues.TAG
import android.util.Log
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
    private val currentUserId: String,
    private val onProposalClick: (ChatMessage) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_MESSAGE = 1
        private const val VIEW_TYPE_PROPOSAL = 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_MESSAGE -> {
                val view = inflater.inflate(R.layout.item_chat_message, parent, false)
                MessageViewHolder(view)
            }
            VIEW_TYPE_PROPOSAL -> {
                val view = inflater.inflate(R.layout.item_proposal_message, parent, false)
                ProposalViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = chatMessages[position]
        Log.d(TAG, "Binding message")
        when (holder.itemViewType) {
            VIEW_TYPE_MESSAGE -> {
                (holder as MessageViewHolder).bind(message)
            }

            VIEW_TYPE_PROPOSAL -> {
                (holder as ProposalViewHolder).bind(message, onProposalClick)
            }
        }
    }
    override fun getItemCount(): Int = chatMessages.size

    override fun getItemViewType(position: Int): Int {
        val message = chatMessages[position]
        return if (message.itinerary != null) VIEW_TYPE_PROPOSAL else VIEW_TYPE_MESSAGE
    }

    class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val messageTextView: TextView = view.findViewById(R.id.message_text)

        fun bind(chatMessage: ChatMessage) {
            messageTextView.text = chatMessage.text
            // Additional binding logic for regular messages if needed
        }
    }

    class ProposalViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val proposalTitleTextView: TextView = view.findViewById(R.id.proposal_title_text_view)

        fun bind(chatMessage: ChatMessage, onProposalClick: (ChatMessage) -> Unit) {
            proposalTitleTextView.text = chatMessage.itinerary?.title
            proposalTitleTextView.setOnClickListener {
                onProposalClick(chatMessage)
            }
        }
    }

}