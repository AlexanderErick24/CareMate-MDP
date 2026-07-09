package com.mdp.caremate.ui.chat

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.mdp.caremate.R
import com.mdp.caremate.data.repositories.AuthRepositoryImpl

import com.mdp.caremate.data.repositories.ChatRepositoryImpl
import com.mdp.caremate.data.sources.remote.FirebaseSource
import com.mdp.caremate.ui.auth.AuthViewModel
import com.mdp.caremate.ui.auth.AuthViewModelFactory

import com.mdp.caremate.ui.chat.adapter.ChatAdapter

class ChatFragment :
    Fragment(R.layout.fragment_chat) {

    private lateinit var viewModel:
            ChatViewModel

    private lateinit var authViewModel:
            AuthViewModel

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {

        super.onViewCreated(
            view,
            savedInstanceState
        )

        val rvChat =
            view.findViewById<RecyclerView>(
                R.id.rvChat
            )

        val etMessage =
            view.findViewById<EditText>(
                R.id.etMessage
            )

        val btnSend =
            view.findViewById<ImageButton>(
                R.id.btnSend
            )

        val caregiverName =
            view.findViewById<TextView>(
                R.id.tvCaregiverName
            )

        val tvFamilyMembers =
            view.findViewById<TextView>(
                R.id.tvFamilyMembers
            )

        val adapter =
            ChatAdapter()

        rvChat.adapter =
            adapter

        rvChat.layoutManager =
            LinearLayoutManager(
                requireContext()
            )

        val repository =
            ChatRepositoryImpl(
                FirebaseSource()
            )

        val factory =
            ChatViewModelFactory(
                repository
            )

        viewModel =
            ViewModelProvider(
                this,
                factory
            )[ChatViewModel::class.java]

        val authRepository =
            AuthRepositoryImpl(
                FirebaseSource()
            )

        val authFactory =
            AuthViewModelFactory(
                authRepository
            )

        authViewModel =
            ViewModelProvider(
                this,
                authFactory
            )[AuthViewModel::class.java]

        viewModel.loadChatRoom()

        authViewModel.getCurrentUser()

        authViewModel
            .getLinkedCaregiver()

        authViewModel.getFamilyMembers()

        viewModel.messages.observe(
            viewLifecycleOwner
        ) {

            adapter.submitList(it)

            rvChat.scrollToPosition(
                it.size - 1
            )
        }

        authViewModel.currentUser.observe(
            viewLifecycleOwner
        ) { user ->

            if (
                user.role.equals(
                    "caregiver",
                    ignoreCase = true
                )
            ) {

                caregiverName.text =
                    user.name
            }
        }

        authViewModel
            .linkedCaregiver
            .observe(
                viewLifecycleOwner
            ) {

                caregiverName.text =
                    it.name
            }

        authViewModel.familyMembers.observe(
            viewLifecycleOwner
        ) {

            tvFamilyMembers.text =
                "Family Members: " +
                        it.joinToString(", ")
        }

        btnSend.setOnClickListener {

            val message =
                etMessage.text
                    .toString()
                    .trim()

            if (
                message.isNotEmpty()
            ) {

                viewModel.sendMessage(
                    message
                )

                etMessage.text.clear()
            }
        }
    }
}