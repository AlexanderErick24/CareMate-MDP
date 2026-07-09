//    package com.mdp.caremate.ui.admin
//
//    import android.app.DatePickerDialog
//    import android.app.TimePickerDialog
//    import android.os.Bundle
//    import androidx.fragment.app.Fragment
//    import android.view.LayoutInflater
//    import android.view.View
//    import android.view.ViewGroup
//    import android.widget.Toast
//    import androidx.fragment.app.viewModels
//    import com.mdp.caremate.R
//    import com.mdp.caremate.data.model.Event
//    import com.mdp.caremate.databinding.FragmentEventFormBinding
//    import java.text.SimpleDateFormat
//    import java.util.Calendar
//    import java.util.Locale
//
//    class EventFormFragment : Fragment() {
//
//        private var _binding: FragmentEventFormBinding? = null
//        private val binding get() = _binding!!
//
//        private val viewModel: EventFormViewModel by viewModels()
//
//        private val calendar = Calendar.getInstance()
//        private var currentEventId: String? = null
//
//        // Variabel lokal untuk menyimpan status listed bawaan data lama
//        private var isCurrentlyListed: Boolean = false
//
//        override fun onCreateView(
//            inflater: LayoutInflater, container: ViewGroup?,
//            savedInstanceState: Bundle?
//        ): View {
//            _binding = FragmentEventFormBinding.inflate(inflater, container, false)
//            return binding.root
//        }
//
//        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//            super.onViewCreated(view, savedInstanceState)
//
//            setupPickers()
//            setupActionButtons()
//            setupObservers()
//            checkEditMode()
//        }
//
//        private fun setupPickers() {
//            val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
//                calendar.set(Calendar.YEAR, year)
//                calendar.set(Calendar.MONTH, month)
//                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
//
//                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
//                binding.etEventDate.setText(dateFormat.format(calendar.time))
//            }
//
//            binding.etEventDate.setOnClickListener {
//                DatePickerDialog(
//                    requireContext(),
//                    dateSetListener,
//                    calendar.get(Calendar.YEAR),
//                    calendar.get(Calendar.MONTH),
//                    calendar.get(Calendar.DAY_OF_MONTH)
//                ).show()
//            }
//
//            val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
//                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
//                calendar.set(Calendar.MINUTE, minute)
//
//                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
//                binding.etEventTime.setText(timeFormat.format(calendar.time))
//            }
//
//            binding.etEventTime.setOnClickListener {
//                TimePickerDialog(
//                    requireContext(),
//                    timeSetListener,
//                    calendar.get(Calendar.HOUR_OF_DAY),
//                    calendar.get(Calendar.MINUTE),
//                    true
//                ).show()
//            }
//        }
//
//        private fun setupActionButtons() {
//            // TOMBOL 1: SAVE (Menyimpan data tanpa mengubah status asli)
//            binding.btnSaveEvent.setOnClickListener {
//                sendEventData(targetListedStatus = isCurrentlyListed)
//            }
//
//            // TOMBOL 2: POST KE PUBLIK (Memaksa status listed menjadi true)
//            binding.btnPost.setOnClickListener {
//                sendEventData(targetListedStatus = true)
//            }
//
//            // TOMBOL 3: HAPUS EVENT
//            binding.btnDeleteEvent.setOnClickListener {
//                val id = currentEventId
//                if (id != null) {
//                    // Minta ViewModel untuk memproses penghapusan ke database
//                    viewModel.deleteEvent(id)
//                } else {
//                    Toast.makeText(requireContext(), "Gagal menghapus: ID Event tidak ditemukan", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//
//        // Helper untuk menghemat penulisan parsing data teks input
//        private fun sendEventData(targetListedStatus: Boolean) {
//            val name = binding.etEventName.text.toString().trim()
//            val date = binding.etEventDate.text.toString().trim()
//            val time = binding.etEventTime.text.toString().trim()
//            val place = binding.etEventPlace.text.toString().trim()
//            val capacity = binding.etEventCapacity.text.toString().trim()
//            val selectedTimestamp = calendar.timeInMillis
//
//            viewModel.saveEvent(currentEventId, name, date, time, place, capacity, selectedTimestamp, targetListedStatus)
//        }
//
//        private fun setupObservers() {
//            viewModel.toastMessage.observe(viewLifecycleOwner) { message ->
//                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
//            }
//
//            viewModel.isActionSuccess.observe(viewLifecycleOwner) { isSuccess ->
//                if (isSuccess) {
//                    parentFragmentManager.popBackStack()
//                }
//            }
//        }
//
//        private fun checkEditMode() {
//            val argsEvent = arguments?.getParcelable<Event>("EXTRA_EVENT")
//
//            if (argsEvent != null) {
//                currentEventId = argsEvent.eid
//                binding.tvEventFormTitle.text = "        Ubah rincian Event"
//
//                binding.etEventName.setText(argsEvent.name)
//                binding.etEventDate.setText(argsEvent.date)
//                binding.etEventTime.setText(argsEvent.time)
//                binding.etEventPlace.setText(argsEvent.place)
//                binding.etEventCapacity.setText(argsEvent.capacity)
//
//                isCurrentlyListed = argsEvent.listed
//
//                // Atur visibilitas tombol berdasarkan status terdaftar (listed) saat ini
//                binding.btnDeleteEvent.visibility = View.VISIBLE
//
//                if (!isCurrentlyListed) {
//                    // Jika masih draft, tampilkan tombol Post ke Publik
//                    binding.btnPost.visibility = View.VISIBLE
//                } else {
//                    // Jika sudah live/listed, sembunyikan tombol Post karena sudah publik
//                    binding.btnPost.visibility = View.GONE
//                }
//
//                try {
//                    val fullDateTimeStr = "${argsEvent.date} ${argsEvent.time}"
//                    val parser = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
//                    val savedDate = parser.parse(fullDateTimeStr)
//
//                    if (savedDate != null) {
//                        calendar.time = savedDate
//                    }
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                }
//            } else {
//                // Mode Tambah Baru: Hanya ada tombol Save, tombol lainnya disembunyikan
//                binding.btnDeleteEvent.visibility = View.GONE
//                binding.btnPost.visibility = View.GONE
//                isCurrentlyListed = false
//            }
//        }
//
//        override fun onDestroyView() {
//            super.onDestroyView()
//            _binding = null
//        }
//    }

package com.mdp.caremate.ui.admin

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import coil.load
import com.mdp.caremate.R
import com.mdp.caremate.data.model.Event
import com.mdp.caremate.databinding.FragmentEventFormBinding
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EventFormFragment : Fragment() {

    private var _binding: FragmentEventFormBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EventFormViewModel by viewModels()

    private val calendar = Calendar.getInstance()
    private var currentEventId: String? = null

    private var isCurrentlyListed: Boolean = false

    // Variabel lokal untuk menyimpan string Base64 gambar event
    private var photoBase64Str: String? = null

    // Launcher untuk mengambil gambar dari galeri
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { processAndPreviewPhoto(it) }
    }

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
        // Aksi klik container/imageview foto event
        binding.flEventPhotoContainer.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnSaveEvent.setOnClickListener {
            sendEventData(targetListedStatus = isCurrentlyListed)
        }

        binding.btnPost.setOnClickListener {
            sendEventData(targetListedStatus = true)
        }

        binding.btnDeleteEvent.setOnClickListener {
            val id = currentEventId
            if (id != null) {
                viewModel.deleteEvent(id)
            } else {
                Toast.makeText(requireContext(), "Gagal menghapus: ID Event tidak ditemukan", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Mengubah URI gambar dari galeri menjadi Base64 string & menampilkan previewnya
    private fun processAndPreviewPhoto(uri: Uri) {
        try {
            val inputStream: InputStream? = requireContext().contentResolver.openInputStream(uri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            if (originalBitmap != null) {
                val maxDim = 500
                val scale = maxDim.toFloat() / maxOf(originalBitmap.width, originalBitmap.height)
                val scaledBitmap = if (scale < 1f) {
                    Bitmap.createScaledBitmap(originalBitmap, (originalBitmap.width * scale).toInt(), (originalBitmap.height * scale).toInt(), true)
                } else {
                    originalBitmap
                }
                val outputStream = ByteArrayOutputStream()
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 75, outputStream)

                // Simpan string base64 ke variabel lokal
                photoBase64Str = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)

                // Tampilkan langsung bitmap ke ImageView preview form
                binding.ivEventPhoto.setImageBitmap(scaledBitmap)
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Gagal memproses gambar", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendEventData(targetListedStatus: Boolean) {
        val name = binding.etEventName.text.toString().trim()
        val date = binding.etEventDate.text.toString().trim()
        val time = binding.etEventTime.text.toString().trim()
        val place = binding.etEventPlace.text.toString().trim()
        val capacity = binding.etEventCapacity.text.toString().trim()
        val selectedTimestamp = calendar.timeInMillis

        // Kirimkan juga parameter photoBase64Str ke ViewModel
        viewModel.saveEvent(currentEventId, name, date, time, place, capacity, selectedTimestamp, targetListedStatus, photoBase64Str)
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
            binding.tvEventFormTitle.text = "        Ubah rincian Event"

            binding.etEventName.setText(argsEvent.name)
            binding.etEventDate.setText(argsEvent.date)
            binding.etEventTime.setText(argsEvent.time)
            binding.etEventPlace.setText(argsEvent.place)
            binding.etEventCapacity.setText(argsEvent.capacity)

            isCurrentlyListed = argsEvent.listed

            // Ambil photoUrl lama jika dalam mode Edit agar tidak ter-reset kosong saat save biasa
            photoBase64Str = argsEvent.photoUrl

            // Tampilkan foto event lama jika ada string Base64-nya
            if (!argsEvent.photoUrl.isNullOrEmpty()) {
                try {
                    val decodedBytes = Base64.decode(argsEvent.photoUrl, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                    if (bitmap != null) {
                        binding.ivEventPhoto.setImageBitmap(bitmap)
                    } else {
                        binding.ivEventPhoto.load(argsEvent.photoUrl)
                    }
                } catch (e: Exception) {
                    binding.ivEventPhoto.load(argsEvent.photoUrl)
                }
            } else {
                binding.ivEventPhoto.setImageResource(android.R.drawable.ic_menu_gallery)
            }

            binding.btnDeleteEvent.visibility = View.VISIBLE

            if (!isCurrentlyListed) {
                binding.btnPost.visibility = View.VISIBLE
            } else {
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
            binding.btnDeleteEvent.visibility = View.GONE
            binding.btnPost.visibility = View.GONE
            binding.ivEventPhoto.setImageResource(android.R.drawable.ic_menu_gallery)
            isCurrentlyListed = false
            photoBase64Str = null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}