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
import com.google.firebase.auth.FirebaseAuth


class ChatAdapter(
    private val chatMessages: List<ChatMessage>,
    private val currentUserId: String,
    private val onProposalClick: (ChatMessage) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_MESSAGE_SENT = 1
        private const val VIEW_TYPE_MESSAGE_RECEIVED = 2
        private const val VIEW_TYPE_PROPOSAL = 3
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_MESSAGE_SENT -> {
                val view = inflater.inflate(R.layout.item_chat_message_sent, parent, false)
                MessageViewHolder(view)
            }
            VIEW_TYPE_MESSAGE_RECEIVED -> {
                val view = inflater.inflate(R.layout.item_chat_message_received, parent, false)
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
            VIEW_TYPE_MESSAGE_SENT, VIEW_TYPE_MESSAGE_RECEIVED -> {  // Handle both sent and received messages
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
        return when {
            !message.itineraryId.isNullOrEmpty() -> VIEW_TYPE_PROPOSAL
            message.senderId == FirebaseAuth.getInstance().currentUser?.uid -> VIEW_TYPE_MESSAGE_SENT
            else -> VIEW_TYPE_MESSAGE_RECEIVED
        }
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
        //private val proposalDescriptionTextView: TextView = view.findViewById(R.id.proposal_description_text_view) // Assuming you add this view
        private val greenVotesTextView: TextView = view.findViewById(R.id.green_votes_text_view)
        private val redVotesTextView: TextView = view.findViewById(R.id.red_votes_text_view)

        fun bind(chatMessage: ChatMessage, onProposalClick: (ChatMessage) -> Unit) {
            // Use the new fields instead of an Itinerary object
            proposalTitleTextView.text = chatMessage.itineraryTitle ?: "No Title"
            //proposalDescriptionTextView.text = chatMessage.itineraryDescription ?: "No Description"

            proposalTitleTextView.setOnClickListener {
                onProposalClick(chatMessage)
            }

            // Bind the votes to the TextViews
            val greenVotes = chatMessage.votes.values.count { it == "green" }
            val redVotes = chatMessage.votes.values.count { it == "red" }
            greenVotesTextView.text = "Green votes: $greenVotes"
            redVotesTextView.text = "Red votes: $redVotes"
        }
    }


}