package com.mdp.caremate.ui.admin

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.mdp.caremate.data.model.User
import com.mdp.caremate.databinding.FragmentUserFormBinding

class UserFormFragment : Fragment() {

    private var _binding: FragmentUserFormBinding? = null
    private val binding get() = _binding!!

    // Inisialisasi ViewModel menggunakan ktx delegate
    private val viewModel: UserFormViewModel by viewModels()

    private var currentUserId: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Setup Observers (Mendengarkan perubahan dari ViewModel)
        setupObservers()

        // 2. Setup Action Listeners (Klik Tombol)
        binding.btnSaveUser.setOnClickListener {
            val name = binding.etUserName.text.toString().trim()
            val email = binding.etUserEmail.text.toString().trim()
            val role = binding.etUserRole.text.toString().trim()
            val pairingCode = binding.etUserPairingCode.text.toString().trim()
            val caregiverUid = binding.etUserCaregiverUid.text.toString().trim()

            // Teruskan data ke ViewModel untuk diproses
            viewModel.saveUser(currentUserId, name, email, role, pairingCode, caregiverUid)
        }

        binding.btnDeleteUser.setOnClickListener {
            viewModel.deleteUser(currentUserId)
        }
    }

    /**
     * Tempat berkumpulnya fungsi observe LiveData
     */
    private fun setupObservers() {
        // Pemicu Toast / Notifikasi Pesan
        viewModel.toastMessage.observe(viewLifecycleOwner) { message ->
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }

        // Aksi ketika simpan sukses (misal: kembali ke halaman sebelumnya)
        viewModel.isSaveSuccess.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess) {
                parentFragmentManager.popBackStack()
            }
        }

        // Aksi ketika hapus sukses
        viewModel.isDeleteSuccess.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess) {
                parentFragmentManager.popBackStack()
            }
        }
    }

    /**
     * Jika Fragment dibuka untuk MODE EDIT, panggil fungsi ini dari luar/argumen
     */
    fun populateForm(user: User) {
        currentUserId = user.uid
        binding.etUserName.setText(user.name)
        binding.etUserEmail.setText(user.email)
        binding.etUserRole.setText(user.role)
        binding.etUserPairingCode.setText(user.pairingCode)
        binding.etUserCaregiverUid.setText(user.caregiverUid)

        binding.btnDeleteUser.visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}