package com.mdp.caremate.ui.admin

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButton
import com.mdp.caremate.R
import com.mdp.caremate.data.model.User
import com.mdp.caremate.databinding.FragmentUsersBinding

class UsersFragment : Fragment() {

    private var _binding: FragmentUsersBinding? = null
    private val binding get() = _binding!!

    private val viewModel: UserViewModel by viewModels()

    // Menyimpan data asli dari Firebase secara terpisah agar tidak terdistorsi saat filtering
    private val allUsersList = ArrayList<User>()
    private var userAdapter: UserAdapter? = null

    private var currentFilter = "ALL"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUsersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearch()
        setupFilterTabs()
        setupClickListeners()
        observeViewModel()

        if (savedInstanceState == null) {
            viewModel.fetchUsers()
        }
    }

    private fun observeViewModel() {
        viewModel.users.observe(viewLifecycleOwner) { users ->
            if (_binding == null || !isAdded) return@observe

            allUsersList.clear()
            if (!users.isNullOrEmpty()) {
                allUsersList.addAll(users)
            } else {
                Toast.makeText(requireContext(), "Tidak ada data user", Toast.LENGTH_SHORT).show()
            }

            // 1. Update counter dashboard atas dengan data paling fresh dari Firebase
            setupDashboardStats()

            // 2. Jalankan ulang filter & search agar UI List langsung sinkron
            applyFilterAndSearch(binding.etSearch.text.toString())
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (_binding == null) return@observe
            // Implementasikan ProgressBar/Shimmer Anda di sini jika ada
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (_binding == null || !isAdded || message == null) return@observe
            Toast.makeText(requireContext(), "Gagal mengambil data: $message", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupDashboardStats() {
        if (_binding == null) return

        val totalUsers = allUsersList.size
        val totalCaregivers = allUsersList.count { it.role.equals("Caregiver", ignoreCase = true) }
        val totalFamily = allUsersList.count { it.role.equals("Family", ignoreCase = true) }
        val activeNow = allUsersList.count { it.status }

        binding.cardTotalUsers.let { card ->
            card.findViewById<TextView>(R.id.lblTotal)?.let { lbl ->
                val container = lbl.parent as? ViewGroup
                (container?.getChildAt(1) as? TextView)?.text = totalUsers.toString()
            }
        }

        binding.cardCaregivers.let { card ->
            card.findViewById<TextView>(R.id.lblCare)?.let { lbl ->
                val container = lbl.parent as? ViewGroup
                (container?.getChildAt(1) as? TextView)?.text = totalCaregivers.toString()
            }
        }

        binding.cardFamily.let { card ->
            card.findViewById<TextView>(R.id.lblFam)?.let { lbl ->
                val container = lbl.parent as? ViewGroup
                (container?.getChildAt(1) as? TextView)?.text = totalFamily.toString()
            }
        }

        binding.cardActiveNow.let { card ->
            card.findViewById<TextView>(R.id.lblActive)?.let { lbl ->
                val container = lbl.parent as? ViewGroup
                (container?.getChildAt(2) as? TextView)?.text = activeNow.toString()
            }
        }
    }

    private fun setupRecyclerView() {
        // Inisialisasi adapter dengan list kosong di awal
        userAdapter = UserAdapter(ArrayList()) { selectedUser ->
            val bundle = Bundle().apply {
                putParcelable("ARG_USER", selectedUser)
            }
            findNavController().navigate(
                R.id.action_adminUsers_to_userDetail,
                bundle
            )
        }

        binding.rvUserList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = userAdapter
        }
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applyFilterAndSearch(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupFilterTabs() {
        binding.chipAll.setOnClickListener {
            currentFilter = "ALL"
            updateTabUI(binding.chipAll, binding.chipPremium, binding.chipCaregivers, binding.chipFamily, binding.chipInactive)
            applyFilterAndSearch(binding.etSearch.text.toString())
        }

        // Tambahan Baru: Click Listener untuk Chip Premium
        binding.chipPremium.setOnClickListener {
            currentFilter = "PREMIUM"
            updateTabUI(binding.chipPremium, binding.chipAll, binding.chipCaregivers, binding.chipFamily, binding.chipInactive)
            applyFilterAndSearch(binding.etSearch.text.toString())
        }

        binding.chipCaregivers.setOnClickListener {
            currentFilter = "Caregiver"
            updateTabUI(binding.chipCaregivers, binding.chipAll, binding.chipPremium, binding.chipFamily, binding.chipInactive)
            applyFilterAndSearch(binding.etSearch.text.toString())
        }

        binding.chipFamily.setOnClickListener {
            currentFilter = "Family"
            updateTabUI(binding.chipFamily, binding.chipAll, binding.chipPremium, binding.chipCaregivers, binding.chipInactive)
            applyFilterAndSearch(binding.etSearch.text.toString())
        }

        binding.chipInactive.setOnClickListener {
            currentFilter = "INACTIVE"
            updateTabUI(binding.chipInactive, binding.chipAll, binding.chipPremium, binding.chipCaregivers, binding.chipFamily)
            applyFilterAndSearch(binding.etSearch.text.toString())
        }
    }

    private fun updateTabUI(activeButton: MaterialButton, vararg inactiveButtons: MaterialButton) {
        activeButton.setBackgroundColor(Color.parseColor("#2D4A43"))
        activeButton.setTextColor(Color.WHITE)

        for (button in inactiveButtons) {
            button.setBackgroundColor(Color.parseColor("#E5E7EB"))
            button.setTextColor(Color.parseColor("#4B5563"))
        }
    }

    private fun applyFilterAndSearch(query: String) {
        val temporaryFilteredList = ArrayList<User>()

        for (user in allUsersList) {
            val matchesFilter = when (currentFilter) {
                "ALL" -> true
                "INACTIVE" -> !user.status // Akun tersuspensi (tidak aktif)
                "PREMIUM" -> user.isPremium // Memfilter hanya user dengan isPremium: true
                else -> user.role.equals(currentFilter, ignoreCase = true)
            }

            val matchesSearch = user.name.contains(query, ignoreCase = true) ||
                    user.email.contains(query, ignoreCase = true)

            if (matchesFilter && matchesSearch) {
                temporaryFilteredList.add(user)
            }
        }

        // Memperbarui data di dalam adapter secara aman menggunakan method internal adapter
        userAdapter?.updateData(temporaryFilteredList)
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}