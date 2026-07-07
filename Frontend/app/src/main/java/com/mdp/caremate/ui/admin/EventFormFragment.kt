package com.mdp.caremate.ui.admin

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.mdp.caremate.R
import com.mdp.caremate.data.model.Event
import com.mdp.caremate.databinding.FragmentEventFormBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EventFormFragment : Fragment() {

    private var _binding: FragmentEventFormBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EventFormViewModel by viewModels()

    private val calendar = Calendar.getInstance()
    private var currentEventId: String? = null

    // Variabel lokal untuk menyimpan status listed bawaan data lama
    private var isCurrentlyListed: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupPickers()
        setupActionButtons()
        setupObservers()
        checkEditMode()
    }

    private fun setupPickers() {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            binding.etEventDate.setText(dateFormat.format(calendar.time))
        }

        binding.etEventDate.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)

            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            binding.etEventTime.setText(timeFormat.format(calendar.time))
        }

        binding.etEventTime.setOnClickListener {
            TimePickerDialog(
                requireContext(),
                timeSetListener,
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }
    }

    private fun setupActionButtons() {
        // TOMBOL 1: SAVE (Menyimpan data tanpa mengubah status asli)
        binding.btnSaveEvent.setOnClickListener {
            sendEventData(targetListedStatus = isCurrentlyListed)
        }

        // TOMBOL 2: POST KE PUBLIK (Memaksa status listed menjadi true)
        binding.btnPost.setOnClickListener {
            sendEventData(targetListedStatus = true)
        }

        // TOMBOL 3: HAPUS EVENT
        binding.btnDeleteEvent.setOnClickListener {
            val id = currentEventId
            if (id != null) {
                // Minta ViewModel untuk memproses penghapusan ke database
                viewModel.deleteEvent(id)
            } else {
                Toast.makeText(requireContext(), "Gagal menghapus: ID Event tidak ditemukan", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Helper untuk menghemat penulisan parsing data teks input
    private fun sendEventData(targetListedStatus: Boolean) {
        val name = binding.etEventName.text.toString().trim()
        val date = binding.etEventDate.text.toString().trim()
        val time = binding.etEventTime.text.toString().trim()
        val place = binding.etEventPlace.text.toString().trim()
        val capacity = binding.etEventCapacity.text.toString().trim()
        val selectedTimestamp = calendar.timeInMillis

        viewModel.saveEvent(currentEventId, name, date, time, place, capacity, selectedTimestamp, targetListedStatus)
    }

    private fun setupObservers() {
        viewModel.toastMessage.observe(viewLifecycleOwner) { message ->
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }

        viewModel.isActionSuccess.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess) {
                parentFragmentManager.popBackStack()
            }
        }
    }

    private fun checkEditMode() {
        val argsEvent = arguments?.getParcelable<Event>("EXTRA_EVENT")

        if (argsEvent != null) {
            currentEventId = argsEvent.eid
            binding.tvEventFormTitle.text = "             Ubah rincian Event"

            binding.etEventName.setText(argsEvent.name)
            binding.etEventDate.setText(argsEvent.date)
            binding.etEventTime.setText(argsEvent.time)
            binding.etEventPlace.setText(argsEvent.place)
            binding.etEventCapacity.setText(argsEvent.capacity)

            isCurrentlyListed = argsEvent.listed

            // Atur visibilitas tombol berdasarkan status terdaftar (listed) saat ini
            binding.btnDeleteEvent.visibility = View.VISIBLE

            if (!isCurrentlyListed) {
                // Jika masih draft, tampilkan tombol Post ke Publik
                binding.btnPost.visibility = View.VISIBLE
            } else {
                // Jika sudah live/listed, sembunyikan tombol Post karena sudah publik
                binding.btnPost.visibility = View.GONE
            }

            try {
                val fullDateTimeStr = "${argsEvent.date} ${argsEvent.time}"
                val parser = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                val savedDate = parser.parse(fullDateTimeStr)

                if (savedDate != null) {
                    calendar.time = savedDate
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            // Mode Tambah Baru: Hanya ada tombol Save, tombol lainnya disembunyikan
            binding.btnDeleteEvent.visibility = View.GONE
            binding.btnPost.visibility = View.GONE
            isCurrentlyListed = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}