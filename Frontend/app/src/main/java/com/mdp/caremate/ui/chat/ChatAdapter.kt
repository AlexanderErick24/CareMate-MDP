package com.mdp.caremate.ui.chat.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import androidx.recyclerview.widget.RecyclerView

import com.google.firebase.auth.FirebaseAuth

import com.mdp.caremate.R
import com.mdp.caremate.data.model.ChatMessage

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatAdapter :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {

        private const val VIEW_SENT = 1

        private const val VIEW_RECEIVED = 2
    }

    private var messages =
        emptyList<ChatMessage>()

    fun submitList(
        newMessages: List<ChatMessage>
    ) {

        messages = newMessages

        notifyDataSetChanged()
    }



    override fun getItemViewType(
        position: Int
    ): Int {

        val currentUid =
            FirebaseAuth.getInstance()
                .currentUser?.uid

        return if (
            messages[position].senderId ==
            currentUid
        ) {

            VIEW_SENT

        } else {

            VIEW_RECEIVED
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {

        return if (
            viewType == VIEW_SENT
        ) {

            val view =
                LayoutInflater.from(
                    parent.context
                ).inflate(
                    R.layout.item_chat_sender,
                    parent,
                    false
                )

            SentViewHolder(view)

        } else {

            val view =
                LayoutInflater.from(
                    parent.context
                ).inflate(
                    R.layout.item_chat_received,
                    parent,
                    false
                )

            ReceivedViewHolder(view)
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {

        val message =
            messages[position]

        when(holder) {

            is SentViewHolder ->
                holder.bind(message)

            is ReceivedViewHolder ->
                holder.bind(message)
        }
    }

    override fun getItemCount() =
        messages.size

    class SentViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        fun formatTime(
            timestamp: Long
        ): String {

            return SimpleDateFormat(
                "HH:mm",
                Locale.getDefault()
            ).format(
                Date(timestamp)
            )
        }

        private val tvMessage =
            itemView.findViewById<TextView>(
                R.id.tvMessage
            )

        private val tvTime =
            itemView.findViewById<TextView>(
                R.id.tvTime
            )

        fun bind(
            message: ChatMessage
        ) {

            tvMessage.text =
                message.message

            tvTime.text =
                formatTime(
                    message.timestamp
                )
        }
    }

    class ReceivedViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        private val tvSender =
            itemView.findViewById<TextView>(
                R.id.tvSender
            )

        private val tvMessage =
            itemView.findViewById<TextView>(
                R.id.tvMessage
            )

        private val tvTime =
            itemView.findViewById<TextView>(
                R.id.tvTime
            )

        fun formatTime(
            timestamp: Long
        ): String {

            return SimpleDateFormat(
                "HH:mm",
                Locale.getDefault()
            ).format(
                Date(timestamp)
            )
        }

        fun bind(
            message: ChatMessage
        ) {

            tvSender.text =
                message.senderName

            tvMessage.text =
                message.message

            tvTime.text =
                formatTime(
                    message.timestamp
                )
        }
    }
}