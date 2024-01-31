package com.example.finalyearprojectdm

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UserSelectionAdapter(private val users: List<User>) : RecyclerView.Adapter<UserSelectionAdapter.ViewHolder>() {

    private val selectedUsers = mutableSetOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user_selection, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(users[position])
    }

    override fun getItemCount() = users.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val checkBox: CheckBox = itemView.findViewById(R.id.user_selection_checkbox)
        private val textView: TextView = itemView.findViewById(R.id.user_email)

        fun bind(user: User) {
            textView.text = user.email
            checkBox.isChecked = selectedUsers.contains(user.id)
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedUsers.add(user.id)
                } else {
                    selectedUsers.remove(user.id)
                }
            }
        }
    }

    fun getSelectedUserIds(): List<String> {
        return selectedUsers.toList()
    }
}