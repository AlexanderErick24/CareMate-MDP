package com.mdp.caremate.ui.admin

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.mdp.caremate.R
import com.mdp.caremate.data.model.Event
import com.mdp.caremate.databinding.FragmentManagementEventAdminDashboardBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs

class ManagementEventAdminDashboard : Fragment() {

    private var _binding: FragmentManagementEventAdminDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EventViewModel by viewModels()
    private lateinit var eventAdapter: EventAdapter

    private val fullEventList = ArrayList<Event>()
    private val displayList = ArrayList<Event>()

    // Menyimpan status tab aktif saat ini (Default: SEMUA)
    private var currentFilterTab = "SEMUA"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManagementEventAdminDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearchBar()
        setupClickListeners()
        setupTabFilters() // Setup interaksi tab filter baru

        observeViewModel()

        if (savedInstanceState == null) {
            viewModel.fetchEvents()
        }
    }

    private fun observeViewModel() {
        viewModel.events.observe(viewLifecycleOwner) { events ->
            if (_binding == null || !isAdded) return@observe

            fullEventList.clear()
            if (events.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Tidak ada data event", Toast.LENGTH_SHORT).show()
            } else {
                fullEventList.addAll(events)
            }

            // Jalankan filter gabungan saat data baru masuk dari database
            applyFilterAndSearch()
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (_binding == null) return@observe
        }

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
                    // PERBAIKAN 1: Navigasi ke EventFormFragment dengan membawa data Event untuk di-edit
                    val bundle = Bundle().apply {
                        putParcelable("EXTRA_EVENT", event)
                    }
                    findNavController().navigate(R.id.action_adminEvents_to_eventFormFragment, bundle)
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
                // Panggil filter gabungan setiap kali teks berubah
                applyFilterAndSearch()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupTabFilters() {
        binding.tabSemua.setOnClickListener {
            currentFilterTab = "SEMUA"
            updateTabVisuals(binding.tabSemua)
            applyFilterAndSearch()
        }

        binding.tabMendatang.setOnClickListener {
            currentFilterTab = "MENDATANG"
            updateTabVisuals(binding.tabMendatang)
            applyFilterAndSearch()
        }

        binding.tabLalu.setOnClickListener {
            currentFilterTab = "LALU"
            updateTabVisuals(binding.tabLalu)
            applyFilterAndSearch()
        }

        binding.tabDraft.setOnClickListener {
            currentFilterTab = "DRAFT"
            updateTabVisuals(binding.tabDraft)
            applyFilterAndSearch()
        }
    }

    private fun updateTabVisuals(activeTab: TextView) {
        val tabs = listOf(binding.tabSemua, binding.tabMendatang, binding.tabLalu, binding.tabDraft)

        tabs.forEach { tab ->
            if (tab == activeTab) {
                tab.setBackgroundResource(R.drawable.bg_tab_active)
                tab.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
            } else {
                tab.setBackgroundResource(R.drawable.bg_tab_inactive)
                tab.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray))
            }
        }
    }

    // Inti Logika: Menggabungkan Filter Tab, Pencarian, DAN Urutan Terdekat dari Hari Ini
    private fun applyFilterAndSearch() {
        displayList.clear()

        val cleanQuery = binding.etSearchEvent.text.toString().trim().lowercase(Locale.getDefault())

        // Gunakan format gabungan tanggal dan jam agar pengecekan waktu sangat presisi
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val now = Calendar.getInstance().time

        // Langkah 1: Saring berdasarkan kategori Tab Aktif menggunakan properti `event.listed`
        val categorizedList = fullEventList.filter { event ->
            val isDraft = !event.listed // Jika listed == false, maka ini adalah Draft

            when (currentFilterTab) {
                "MENDATANG" -> {
                    try {
                        val fullDateTimeStr = "${event.date} ${event.time}"
                        val eventDateTime = sdf.parse(fullDateTimeStr)
                        // Harus listed (bukan draft) dan waktunya setelah detik ini
                        eventDateTime != null && eventDateTime.after(now) && !isDraft
                    } catch (e: Exception) {
                        false
                    }
                }
                "LALU" -> {
                    try {
                        val fullDateTimeStr = "${event.date} ${event.time}"
                        val eventDateTime = sdf.parse(fullDateTimeStr)
                        // Harus listed (bukan draft) dan waktunya sebelum detik ini
                        eventDateTime != null && eventDateTime.before(now) && !isDraft
                    } catch (e: Exception) {
                        false
                    }
                }
                "DRAFT" -> {
                    // Hanya ambil yang tidak listed (listed == false)
                    isDraft
                }
                else -> {
                    // "SEMUA" -> Menampilkan semua event baik yang listed maupun draft
                    true
                }
            }
        }

        // Langkah 2: Saring hasil kategori tadi menggunakan Query teks dari SearchBar
        val filteredList = ArrayList<Event>()
        if (cleanQuery.isEmpty()) {
            filteredList.addAll(categorizedList)
        } else {
            for (event in categorizedList) {
                if (event.name.lowercase(Locale.getDefault()).contains(cleanQuery) ||
                    event.place.lowercase(Locale.getDefault()).contains(cleanQuery)) {
                    filteredList.add(event)
                }
            }
        }

        // Langkah 3: Urutkan hasil akhir berdasarkan tanggal event yang PALING DEKAT dengan hari ini (Ascending Selisih Waktu)
        val sortedList = filteredList.sortedBy { event ->
            try {
                val fullDateTimeStr = "${event.date} ${event.time}"
                val eventDateTime = sdf.parse(fullDateTimeStr)
                if (eventDateTime != null) {
                    // Menghitung selisih absolut (milidetik) antara waktu sekarang dan waktu event
                    abs(eventDateTime.time - now.time)
                } else {
                    Long.MAX_VALUE
                }
            } catch (e: Exception) {
                Long.MAX_VALUE // Jika terjadi error parsing tanggal, ditaruh di urutan paling bawah
            }
        }

        // Masukkan hasil filter & urutan ke list tampilan adapter
        displayList.addAll(sortedList)
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