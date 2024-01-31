import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.finalyearprojectdm.GroupChat
import com.example.finalyearprojectdm.R

class GroupChatAdapter(
    private val groupChats: List<GroupChat>,
    private val itemClick: (GroupChat) -> Unit
) : RecyclerView.Adapter<GroupChatAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_group_chat, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(groupChats[position], itemClick)
    }

    override fun getItemCount() = groupChats.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textView: TextView = itemView.findViewById(R.id.group_chat_name)

        fun bind(groupChat: GroupChat, itemClick: (GroupChat) -> Unit) {
            textView.text = groupChat.name
            itemView.setOnClickListener { itemClick(groupChat) }
        }
    }
}