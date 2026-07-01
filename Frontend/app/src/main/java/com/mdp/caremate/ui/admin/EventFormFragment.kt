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

    // Inisialisasi ViewModel
    private val viewModel: EventFormViewModel by viewModels()

    private val calendar = Calendar.getInstance()
    private var currentEventId: String? = null

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

    // 1. Logika untuk memunculkan dialog Tanggal dan Waktu (Tetap berada di Fragment karena bagian dari UI)
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

    // 2. Meneruskan interaksi aksi tombol ke ViewModel
    private fun setupActionButtons() {
        binding.btnSaveEvent.setOnClickListener {
            val name = binding.etEventName.text.toString().trim()
            val date = binding.etEventDate.text.toString().trim()
            val time = binding.etEventTime.text.toString().trim()
            val place = binding.etEventPlace.text.toString().trim()
            val capacity = binding.etEventCapacity.text.toString().trim()

            // Delegasikan proses penyimpanan dan validasi ke ViewModel
            viewModel.saveEvent(currentEventId, name, date, time, place, capacity)
        }

        binding.btnDeleteEvent.setOnClickListener {
            viewModel.deleteEvent(currentEventId)
        }
    }

    // 3. Mengamati state/data perubahan dari ViewModel
    private fun setupObservers() {
        viewModel.toastMessage.observe(viewLifecycleOwner) { message ->
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }

        viewModel.isActionSuccess.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess) {
                parentFragmentManager.popBackStack() // Kembali ke screen sebelumnya jika aksi berhasil
            }
        }
    }

    // 4. Mengecek mode edit data lama
    private fun checkEditMode() {
        // PERBAIKAN: Diubah menjadi getParcelable karena model Event menggunakan @Parcelize
        val argsEvent = arguments?.getParcelable<Event>("EXTRA_EVENT")

        if (argsEvent != null) {
            currentEventId = argsEvent.eid
            binding.tvEventFormTitle.text = "Ubah rincian Event"
            binding.btnDeleteEvent.visibility = View.VISIBLE

            binding.etEventName.setText(argsEvent.name)
            binding.etEventDate.setText(argsEvent.date)
            binding.etEventTime.setText(argsEvent.time)
            binding.etEventPlace.setText(argsEvent.place)
            binding.etEventCapacity.setText(argsEvent.capacity)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}