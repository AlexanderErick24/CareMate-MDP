package com.mdp.caremate.ui.chat

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.mdp.caremate.R
import com.mdp.caremate.data.repositories.AuthRepositoryImpl
import com.mdp.caremate.data.sources.remote.FirebaseSource
import com.mdp.caremate.databinding.FragmentChatRoomListBinding
import com.mdp.caremate.ui.auth.AuthViewModel
import com.mdp.caremate.ui.auth.AuthViewModelFactory

class ChatRoomListFragment : Fragment(R.layout.fragment_chat_room_list) {

    private var _binding: FragmentChatRoomListBinding? = null
    private val binding get() = _binding!!

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(AuthRepositoryImpl(FirebaseSource()))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentChatRoomListBinding.bind(view)

        // Default: Sembunyikan AI Chat
        binding.cvAiChat.visibility = View.GONE

        // Ambil data user yang sedang login
        authViewModel.getCurrentUser()
        authViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user.role.equals("caregiver", ignoreCase = true)) {
                binding.cvAiChat.visibility = View.VISIBLE
            } else {
                binding.cvAiChat.visibility = View.GONE
            }
        }

        // Navigate to Family Chat
        binding.cvFamilyChat.setOnClickListener {
            findNavController().navigate(R.id.action_chatRoomList_to_familyChat)
        }

        // Navigate to AI Journaling Chat
        binding.cvAiChat.setOnClickListener {
            findNavController().navigate(R.id.action_chatRoomList_to_aiChat)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
