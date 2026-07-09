package com.mdp.caremate.ui.community

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import coil.load
import com.mdp.caremate.data.model.Event
import com.mdp.caremate.databinding.FragmentEventDetailBinding

class EventDetailFragment : Fragment() {

    private var _binding: FragmentEventDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EventDetailViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val argsEvent = arguments?.getParcelable<Event>("event")
        if (argsEvent != null) {
            viewModel.setEvent(argsEvent)
        } else {
            val eventId = arguments?.getString(ARG_EVENT_ID).orEmpty()
            if (eventId.isNotEmpty()) {
                val name = arguments?.getString(ARG_EVENT_NAME).orEmpty()
                val date = arguments?.getString(ARG_EVENT_DATE).orEmpty()
                val time = arguments?.getString(ARG_EVENT_TIME).orEmpty()
                val place = arguments?.getString(ARG_EVENT_PLACE).orEmpty()
                val capacity = arguments?.getString(ARG_EVENT_CAPACITY).orEmpty()
                val photoUrl = arguments?.getString(ARG_EVENT_PHOTO_URL).orEmpty()

                viewModel.setEvent(
                    Event(
                        eid = eventId,
                        name = name,
                        date = date,
                        time = time,
                        place = place,
                        capacity = capacity,
                        photoUrl = photoUrl
                    )
                )
            } else {
                val idArg = arguments?.getString("eventId")
                if (idArg != null) {
                    viewModel.loadEventById(idArg)
                }
            }
        }

        observeEvent()
        setupBackButton()
    }

    private fun observeEvent() {
        viewModel.event.observe(viewLifecycleOwner) { event ->
            if (event != null) {
                binding.tvDetailTitle.text = event.name
                binding.tvDetailDate.text = "${event.date} ${event.time}"
                binding.tvDetailLocation.text = event.place
                val capText = if (event.capacity.contains("/") || event.capacity.contains("Kapasitas", ignoreCase = true)) {
                    event.capacity
                } else if (event.capacity.isNotEmpty()) {
                    "${event.capacity} (Terbuka untuk Umum)"
                } else {
                    "Terbuka untuk Umum / Tanpa Batas"
                }
                binding.tvDetailOrganizer.text = capText
                binding.tvDetailDescription.text = "Acara kesehatan komunitas resmi yang diselenggarakan oleh CareMate untuk mendukung kesehatan fisik dan mental para caregiver serta lansia. Silakan hadir tepat waktu sesuai jadwal dan lokasi yang tertera."

                if (!event.photoUrl.isNullOrEmpty()) {
                    binding.cardDetailPoster.visibility = View.VISIBLE
                    try {
                        val decodedBytes = Base64.decode(event.photoUrl, Base64.DEFAULT)
                        val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                        if (bitmap != null) {
                            binding.ivDetailEventPoster.setImageBitmap(bitmap)
                        } else {
                            binding.ivDetailEventPoster.load(event.photoUrl)
                        }
                    } catch (e: Exception) {
                        binding.ivDetailEventPoster.load(event.photoUrl)
                    }
                } else {
                    binding.cardDetailPoster.visibility = View.GONE
                }
            }
        }
    }

    private fun setupBackButton() {
        binding.btnDetailBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        const val ARG_EVENT_ID = "arg_event_id"
        const val ARG_EVENT_NAME = "arg_event_name"
        const val ARG_EVENT_DATE = "arg_event_date"
        const val ARG_EVENT_TIME = "arg_event_time"
        const val ARG_EVENT_PLACE = "arg_event_place"
        const val ARG_EVENT_CAPACITY = "arg_event_capacity"
        const val ARG_EVENT_PHOTO_URL = "arg_event_photo_url"
    }
}
