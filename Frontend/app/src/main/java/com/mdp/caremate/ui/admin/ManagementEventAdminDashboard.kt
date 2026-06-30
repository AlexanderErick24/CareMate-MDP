package com.mdp.caremate.ui.admin

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.mdp.caremate.data.model.Event
import com.mdp.caremate.databinding.FragmentManagementEventAdminDashboardBinding

class ManagementEventAdminDashboard : Fragment() {

    // View Binding setup untuk Fragment
    private var _binding: FragmentManagementEventAdminDashboardBinding? = null
    private val binding get() = _binding!!

    // Variabel untuk Adapter dan List Data
    private lateinit var eventAdapter: EventAdapter // Anda perlu membuat file adapter ini (ada di bawah)
    private var fullEventList = ArrayList<Event>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate layout menggunakan View Binding
        _binding = FragmentManagementEventAdminDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Inisialisasi Data Dummy & RecyclerView
        setupDummyData()
        setupRecyclerView()

        // 2. Setup Aksi Tombol-Tombol Atas (Header)
        binding.btnBack.setOnClickListener {
            // Aksi kembali, contoh jika menggunakan Fragment Manager biasa:
            parentFragmentManager.popBackStack()
        }

        binding.btnNotification.setOnClickListener {
            Toast.makeText(requireContext(), "Membuka Notifikasi", Toast.LENGTH_SHORT).show()
        }

        // 3. Setup Fitur Pencarian (Search Bar EditText)
        setupSearchBar()

        // 4. Setup Floating Action Button (FAB) Tambah Event
        binding.fabAddEvent.setOnClickListener {
            Toast.makeText(requireContext(), "Tambah Event Baru", Toast.LENGTH_SHORT).show()
            // TODO: Pindah ke Fragment/Activity Input Event Baru
        }

    }

    private fun setupRecyclerView() {
        // Init adapter dengan listener klik item jika dibutuhkan
        eventAdapter = EventAdapter(fullEventList) { event ->
            Toast.makeText(requireContext(), "Mengklik: ${event.name}", Toast.LENGTH_SHORT).show()
        }

        binding.rvEvents.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = eventAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupSearchBar() {
        // Menemukan EditText di dalam bungkusan LinearLayout pencarian
        // Karena EditText di XML Anda belum memiliki ID, mari kita buat logic pencarian yang aman
        // Cara terbaik: Buka XML Anda, tambahkan id pada EditText tersebut, misal: android:id="@+id/etSearchEvent"

        // Asumsi jika Anda sudah menambahkan android:id="@+id/etSearchEvent" pada EditText di XML:
        // binding.etSearchEvent.addTextChangedListener(object : TextWatcher { ... })
    }

    private fun setupDummyData() {
        fullEventList.clear()
        fullEventList.add(Event("1", "Donor Darah CareMate", "15 Juni 2026", "09:00", "Surabaya", "100", true))
        fullEventList.add(Event("2", "Seminar IT Consultant", "20 Juni 2026", "13:00", "Kampus ISTTS", "50", true))
        fullEventList.add(Event("3", "Workshop Odoo ERP", "25 Juni 2026", "10:00", "Online Zoom", "200", false))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Bersihkan binding untuk mencegah memory leak
        _binding = null
    }
}