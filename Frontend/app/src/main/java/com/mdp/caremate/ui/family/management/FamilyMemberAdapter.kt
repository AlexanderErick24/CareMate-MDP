package com.mdp.caremate.ui.family.management.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

import com.mdp.caremate.R
import com.mdp.caremate.data.model.FamilyMember

class FamilyMemberAdapter(
    // Called when the "Quit Family" button is pressed for a member
    private val onQuitClick: (FamilyMember) -> Unit = {}
) : RecyclerView.Adapter<FamilyMemberAdapter.ViewHolder>() {

    private var members =
        emptyList<FamilyMember>()

    // Maps familyUid -> quit status text to show per member
    // e.g. "pending" → "Waiting for approval" / "rejected" → "Rejected: <reason>"
    private var quitStatusMap: Map<String, String> = emptyMap()

    fun submitList(
        newList: List<FamilyMember>
    ) {
        members = newList
        notifyDataSetChanged()
    }

    fun setQuitStatusMap(map: Map<String, String>) {
        quitStatusMap = map
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val view =
            LayoutInflater.from(
                parent.context
            ).inflate(
                R.layout.item_family_member,
                parent,
                false
            )

        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.bind(
            members[position],
            quitStatusMap[members[position].uid],
            onQuitClick
        )
    }

    override fun getItemCount() =
        members.size

    class ViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        private val tvName =
            itemView.findViewById<TextView>(R.id.tvName)

        private val tvEmail =
            itemView.findViewById<TextView>(R.id.tvEmail)

        private val tvRole =
            itemView.findViewById<TextView>(R.id.tvRole)

        private val tvQuitStatus =
            itemView.findViewById<TextView>(R.id.tvQuitStatus)

        private val btnQuitFamily =
            itemView.findViewById<MaterialButton>(R.id.btnQuitFamily)

        fun bind(
            member: FamilyMember,
            quitStatus: String?,
            onQuitClick: (FamilyMember) -> Unit
        ) {
            tvName.text = member.name
            tvEmail.text = member.email
            tvRole.text = "Family Member"

            if (quitStatus != null) {
                tvQuitStatus.visibility = View.VISIBLE
                tvQuitStatus.text = quitStatus
                // Disable the button while a request is already pending
                btnQuitFamily.isEnabled = false
                btnQuitFamily.alpha = 0.5f
            } else {
                tvQuitStatus.visibility = View.GONE
                btnQuitFamily.isEnabled = true
                btnQuitFamily.alpha = 1.0f
            }

            btnQuitFamily.setOnClickListener {
                onQuitClick(member)
            }
        }
    }
}