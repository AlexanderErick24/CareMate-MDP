package com.mdp.caremate.ui.admin

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels // Butuh library fragment-ktx untuk delegasi ini
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.mdp.caremate.R
import com.mdp.caremate.data.model.Event
import com.mdp.caremate.databinding.FragmentManagementEventAdminDashboardBinding
import java.util.Locale

class ManagementEventAdminDashboard : Fragment() {

    private var _binding: FragmentManagementEventAdminDashboardBinding? = null
    private val binding get() = _binding!!

    // Inisialisasi ViewModel secara bersih menggunakan Jetpack ktx delegasi
    private val viewModel: EventViewModel by viewModels()

    private lateinit var eventAdapter: EventAdapter

    private val fullEventList = ArrayList<Event>()
    private val displayList = ArrayList<Event>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManagementEventAdminDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Inisialisasi Komponen Komponen Utama UI
        setupRecyclerView()
        setupSearchBar()
        setupClickListeners()

        // 2. Hubungkan Pengamat (Observer) ke ViewModel
        observeViewModel()

        // 3. Tarik data dari database (Hanya dijalankan saat pertama kali halaman dibuat)
        if (savedInstanceState == null) {
            viewModel.fetchEvents()
        }
    }

    private fun observeViewModel() {
        // Mengamati perubahan data list event
        viewModel.events.observe(viewLifecycleOwner) { events ->
            if (_binding == null || !isAdded) return@observe

            fullEventList.clear()
            if (events.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Tidak ada data event", Toast.LENGTH_SHORT).show()
            } else {
                fullEventList.addAll(events)
            }

            // Jalankan filter pencarian sinkron dengan teks di SearchBar saat ini
            applySearch(binding.etSearchEvent.text.toString())
        }

        // Mengamati state loading
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (_binding == null) return@observe
            // Anda bisa menyalakan ProgressBar/Shimmer di sini jika ada di XML layout Anda
        }

        // Mengamati jika ada error dari sistem database Firebase
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            if (_binding == null || !isAdded || message == null) return@observe
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        }
    }

    private fun setupRecyclerView() {
        eventAdapter = EventAdapter(
            displayList,
            onEditClick = { event ->
                if (isAdded) {
                    Toast.makeText(requireContext(), "Edit: ${event.name}", Toast.LENGTH_SHORT).show()
                }
            },
            onPesertaClick = { event ->
                if (isAdded) {
                    Toast.makeText(requireContext(), "Melihat peserta: ${event.name}", Toast.LENGTH_SHORT).show()
                }
            }
        )

        binding.rvEvents.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = eventAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupSearchBar() {
        binding.etSearchEvent.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                applySearch(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun applySearch(query: String) {
        displayList.clear()
        val cleanQuery = query.trim().lowercase(Locale.getDefault())

        if (cleanQuery.isEmpty()) {
            displayList.addAll(fullEventList)
        } else {
            for (event in fullEventList) {
                if (event.name.lowercase(Locale.getDefault()).contains(cleanQuery) ||
                    event.place.lowercase(Locale.getDefault()).contains(cleanQuery)) {
                    displayList.add(event)
                }
            }
        }

        // Jika EventAdapter Anda memegang fungsi pembantu custom untuk update list seperti updateList()
        // Anda juga bisa memanggilnya di sini: eventAdapter.updateList(displayList)
        eventAdapter.notifyDataSetChanged()
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.btnNotification.setOnClickListener {
            Toast.makeText(requireContext(), "Membuka Notifikasi", Toast.LENGTH_SHORT).show()
        }

        binding.fabAddEvent.setOnClickListener {
            findNavController().navigate(R.id.action_adminEvents_to_eventFormFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}