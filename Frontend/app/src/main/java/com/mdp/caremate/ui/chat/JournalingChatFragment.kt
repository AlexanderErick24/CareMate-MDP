package com.mdp.caremate.ui.chat

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mdp.caremate.R
import com.mdp.caremate.data.repositories.AuthRepositoryImpl
import com.mdp.caremate.data.sources.remote.FirebaseSource
import com.mdp.caremate.ui.auth.AuthViewModel
import com.mdp.caremate.ui.auth.AuthViewModelFactory
import com.mdp.caremate.ui.premium.PremiumViewModel
import com.mdp.caremate.ui.premium.PremiumViewModelFactory

import androidx.navigation.fragment.findNavController

class JournalingChatFragment : Fragment(R.layout.fragment_journaling_chat) {

    private val viewModel by viewModels<PremiumViewModel> { PremiumViewModelFactory }
    private val authViewModel by viewModels<AuthViewModel> {
        AuthViewModelFactory(AuthRepositoryImpl(FirebaseSource()))
    }
    
    private lateinit var adapter: JournalingChatAdapter
    private var currentCaregiverId: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Cek status premium per-user
        val isPremium = com.mdp.caremate.utils.PremiumUtils.isPremium(requireContext())
        if (!isPremium) {
            Toast.makeText(requireContext(), "Fitur AI Mindful Journaling hanya untuk pengguna Premium", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.paywallFragment)
            return
        }

        val rvChat = view.findViewById<RecyclerView>(R.id.rvChat)
        val etMessage = view.findViewById<EditText>(R.id.etMessage)
        val btnSend = view.findViewById<ImageButton>(R.id.btnSend)

        adapter = JournalingChatAdapter()
        rvChat.adapter = adapter
        rvChat.layoutManager = LinearLayoutManager(requireContext())

        // Dapatkan data user yang login
        authViewModel.getCurrentUser()
        authViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            currentCaregiverId = user.uid
            // Load history ONLY when we know the caregiverId
            viewModel.initHistory(currentCaregiverId)
        }

        viewModel.journals.observe(viewLifecycleOwner) { journals ->
            val chatMessages = mutableListOf<AiChatMessage>()
            
            // Map the history into individual chat bubbles (User msg + AI msg)
            for (journal in journals) {
                // 1. User Message
                chatMessages.add(
                    AiChatMessage(
                        id = journal.id + "_user",
                        text = journal.content,
                        isFromUser = true,
                        timestamp = journal.timestamp
                    )
                )
                // 2. AI Message (if analysis exists and is not empty)
                if (journal.aiAnalysis.isNotBlank()) {
                    chatMessages.add(
                        AiChatMessage(
                            id = journal.id + "_ai",
                            text = journal.aiAnalysis,
                            isFromUser = false,
                            timestamp = journal.timestamp + 1000 // Add small delay so it sorts properly if needed
                        )
                    )
                }
            }
            
            adapter.submitList(chatMessages)
            if (chatMessages.isNotEmpty()) {
                rvChat.scrollToPosition(chatMessages.size - 1)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            btnSend.isEnabled = !isLoading
        }

        viewModel.burnoutAlert.observe(viewLifecycleOwner) { isBurnout ->
            if (isBurnout) {
                showBurnoutAlert()
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMsg ->
            if (errorMsg != null) {
                Toast.makeText(requireContext(), "Error: $errorMsg", Toast.LENGTH_LONG).show()
            }
        }

        btnSend.setOnClickListener {
            val message = etMessage.text.toString().trim()
            if (message.isNotEmpty() && currentCaregiverId.isNotEmpty()) {
                // Jangan hapus list (jangan pakai emptyList), tapi tambahkan pesan baru secara manual ke UI
                val currentList = adapter.getCurrentList().toMutableList()
                
                // 1. Pesan User
                currentList.add(
                    AiChatMessage(
                        id = "temp_user_${System.currentTimeMillis()}",
                        text = message,
                        isFromUser = true,
                        timestamp = System.currentTimeMillis()
                    )
                )
                // 2. Indikator AI Mengetik
                currentList.add(
                    AiChatMessage(
                        id = "temp_ai_loading",
                        text = "Teman AI sedang memikirkan balasan...",
                        isFromUser = false,
                        timestamp = System.currentTimeMillis() + 1
                    )
                )
                
                adapter.submitList(currentList)
                rvChat.scrollToPosition(currentList.size - 1)
                
                viewModel.analyzeJournal(message, currentCaregiverId)
                etMessage.text.clear()
            }
        }
    }

    private fun showBurnoutAlert() {
        AlertDialog.Builder(requireContext())
            .setTitle("⚠️ Peringatan Kelelahan")
            .setMessage("Sistem mendeteksi tingkat stres Anda sedang tinggi. Sebagai Caregiver, kesehatan Anda juga sangat penting.\n\nSaran: Mohon ambil waktu istirahat sejenak, atau minta bantuan anggota keluarga lain untuk menjaga pasien hari ini.")
            .setPositiveButton("Baik, Saya Mengerti") { dialog, _ ->
                dialog.dismiss()
            }
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }
}
