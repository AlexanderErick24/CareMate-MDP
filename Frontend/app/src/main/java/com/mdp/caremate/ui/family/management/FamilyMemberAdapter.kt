package com.mdp.caremate.ui.family.management.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import androidx.recyclerview.widget.RecyclerView

import com.mdp.caremate.R
import com.mdp.caremate.data.model.FamilyMember

class FamilyMemberAdapter :
    RecyclerView.Adapter<FamilyMemberAdapter.ViewHolder>() {

    private var members =
        emptyList<FamilyMember>()

    fun submitList(
        newList: List<FamilyMember>
    ) {

        members = newList

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
            members[position]
        )
    }

    override fun getItemCount() =
        members.size

    class ViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        private val tvName =
            itemView.findViewById<TextView>(
                R.id.tvName
            )

        private val tvEmail =
            itemView.findViewById<TextView>(
                R.id.tvEmail
            )

        private val tvRole =
            itemView.findViewById<TextView>(
                R.id.tvRole
            )

        fun bind(
            member: FamilyMember
        ) {

            tvName.text =
                member.name

            tvEmail.text =
                member.email

            tvRole.text =
                "Family Member"
        }
    }
}