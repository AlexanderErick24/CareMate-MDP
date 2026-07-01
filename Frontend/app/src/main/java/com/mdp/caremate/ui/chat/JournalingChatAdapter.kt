package com.mdp.caremate.ui.chat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mdp.caremate.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AiChatMessage(
    val id: String,
    val text: String,
    val isFromUser: Boolean,
    val timestamp: Long
)

class JournalingChatAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_SENT = 1
        private const val VIEW_RECEIVED = 2
    }

    private var messages = emptyList<AiChatMessage>()

    fun getCurrentList(): List<AiChatMessage> = messages

    fun submitList(newMessages: List<AiChatMessage>) {
        messages = newMessages
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isFromUser) VIEW_SENT else VIEW_RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_SENT) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_sender, parent, false)
            SentViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_received, parent, false)
            ReceivedViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        when (holder) {
            is SentViewHolder -> holder.bind(message)
            is ReceivedViewHolder -> holder.bind(message)
        }
    }

    override fun getItemCount() = messages.size

    class SentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMessage = itemView.findViewById<TextView>(R.id.tvMessage)
        private val tvTime = itemView.findViewById<TextView>(R.id.tvTime)

        fun bind(message: AiChatMessage) {
            tvMessage.text = message.text
            tvTime.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))
        }
    }

    class ReceivedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvSender = itemView.findViewById<TextView>(R.id.tvSender)
        private val tvMessage = itemView.findViewById<TextView>(R.id.tvMessage)
        private val tvTime = itemView.findViewById<TextView>(R.id.tvTime)

        fun bind(message: AiChatMessage) {
            tvSender.text = "Teman AI"
            tvMessage.text = message.text
            tvTime.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp))
        }
    }
}
