package com.mdp.caremate.ui.chat

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.mdp.caremate.R
import com.mdp.caremate.data.repositories.AuthRepositoryImpl
import com.mdp.caremate.data.repositories.ChatRepositoryImpl
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

    private lateinit var chatViewModel: ChatViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentChatRoomListBinding.bind(view)

        // =========================
        // SETUP CHAT VIEWMODEL
        // =========================

        val chatRepository = ChatRepositoryImpl(FirebaseSource())
        val chatFactory = ChatViewModelFactory(chatRepository)
        chatViewModel = ViewModelProvider(
            this,
            chatFactory
        )[ChatViewModel::class.java]

        // Load the room — this starts observeRoom() so we get live badge updates
        chatViewModel.loadChatRoom()

        // =========================
        // EXISTING LOGIC
        // =========================

        // Default: hide AI Chat
        binding.cvAiChat.visibility = View.GONE

        authViewModel.getCurrentUser()
        authViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user.role.equals("caregiver", ignoreCase = true)) {
                binding.cvAiChat.visibility = View.VISIBLE
            } else {
                binding.cvAiChat.visibility = View.GONE
            }
        }

        binding.cvFamilyChat.setOnClickListener {
            findNavController().navigate(R.id.action_chatRoomList_to_familyChat)
        }

        binding.cvAiChat.setOnClickListener {
            findNavController().navigate(R.id.action_chatRoomList_to_aiChat)
        }

        // =========================
        // NEW: UNREAD BADGE LOGIC
        // =========================

        val currentUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        // Observe lastSenderId and unreadCount together.
        // We use a small helper to decide if badge should show:
        // → show badge only when lastSenderId is SOMEONE ELSE (not me)
        //   AND unreadCount > 0.

        chatViewModel.unreadCount.observe(viewLifecycleOwner) { count ->
            val lastSender = chatViewModel.lastSenderId.value ?: ""
            updateBadge(count, lastSender, currentUid)
        }

        chatViewModel.lastSenderId.observe(viewLifecycleOwner) { lastSender ->
            val count = chatViewModel.unreadCount.value ?: 0
            updateBadge(count, lastSender, currentUid)
        }
    }

    private fun updateBadge(
        unreadCount: Int,
        lastSenderId: String,
        currentUid: String
    ) {

        // Only show badge if the last message was sent by the OTHER person
        val hasUnread = unreadCount > 0 && lastSenderId != currentUid

        // -- Badge on the Family Chat card --
        if (hasUnread) {
            binding.tvUnreadBadge.visibility = View.VISIBLE
            binding.tvUnreadBadge.text =
                if (unreadCount > 99) "99+" else unreadCount.toString()
        } else {
            binding.tvUnreadBadge.visibility = View.GONE
        }

        // -- Badge on the Bottom Navigation Chat item --
        // For CAREGIVER: the BottomNavigationView is in MainActivity (id: bottom_nav)
        // For FAMILY: the BottomNavigationView is in FamilyContainerFragment (id: familyBottomNav)
        // We try both — whichever is found in the view hierarchy wins.
        updateBottomNavBadge(hasUnread)
    }

    private fun updateBottomNavBadge(hasUnread: Boolean) {

        // Try the caregiver bottom nav (in the Activity)
        val caregiverBottomNav =
            activity?.findViewById<BottomNavigationView>(R.id.bottom_nav)

        // Try the family bottom nav (in the parent Fragment container)
        val familyBottomNav =
            parentFragment?.parentFragment
                ?.view?.findViewById<BottomNavigationView>(R.id.familyBottomNav)
                ?: requireActivity()
                    .findViewById<BottomNavigationView>(R.id.familyBottomNav)

        val bottomNav = caregiverBottomNav ?: familyBottomNav

        if (bottomNav != null) {
            val badge = bottomNav.getOrCreateBadge(R.id.chatFragment)
            if (hasUnread) {
                badge.isVisible = true
            } else {
                badge.isVisible = false
                bottomNav.removeBadge(R.id.chatFragment)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}