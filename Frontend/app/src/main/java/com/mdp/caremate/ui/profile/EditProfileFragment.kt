package com.mdp.caremate.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.mdp.caremate.data.repositories.ProfileRepositoryImpl
import com.mdp.caremate.data.sources.remote.FirebaseSource
import com.mdp.caremate.databinding.FragmentEditProfileBinding

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val firebaseSource = FirebaseSource()
        val repository = ProfileRepositoryImpl(firebaseSource)
        val factory = ProfileViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[ProfileViewModel::class.java]

        setupObservers()
        viewModel.fetchCurrentUser()

        binding.btnSaveProfile.setOnClickListener {
            val name = binding.etProfileName.text.toString().trim()
            val jobTitle = binding.etProfileJob.text.toString().trim()
            val age = binding.etProfileAge.text.toString().trim().toIntOrNull() ?: 0
            val bio = binding.etProfileBio.text.toString().trim()
            
            val expLines = binding.etProfileExp.text.toString()
                .split("\n")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                
            val skillsList = binding.etProfileSkills.text.toString()
                .split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }

            if (name.isEmpty()) {
                Toast.makeText(requireContext(), "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.updateUserProfile(name, jobTitle, age, bio, expLines, skillsList)
        }
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.btnSaveProfile.isEnabled = !isLoading
            binding.btnSaveProfile.text = if (isLoading) "Menyimpan..." else "Simpan Perubahan"
        }

        viewModel.toastMessage.observe(viewLifecycleOwner) { msg ->
            if (msg != null) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.userState.observe(viewLifecycleOwner) { user ->
            if (user != null && binding.etProfileName.text.isNullOrEmpty()) {
                binding.etProfileName.setText(user.name)
                binding.etProfileJob.setText(user.jobTitle)
                if (user.age > 0) binding.etProfileAge.setText(user.age.toString())
                binding.etProfileBio.setText(user.bio)
                binding.etProfileExp.setText(user.experience.joinToString("\n"))
                binding.etProfileSkills.setText(user.skills.joinToString(", "))
            }
        }

        viewModel.updateSuccess.observe(viewLifecycleOwner) { success ->
            if (success == true) {
                viewModel.resetUpdateSuccess()
                findNavController().popBackStack()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
