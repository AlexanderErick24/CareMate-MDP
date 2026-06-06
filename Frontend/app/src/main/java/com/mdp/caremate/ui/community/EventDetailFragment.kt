package com.mdp.caremate.ui.community

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
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

        // Ambil data dari Bundle yang dikirim CommunityFragment
        val eventId = arguments?.getString(ARG_EVENT_ID).orEmpty()
        val title = arguments?.getString(ARG_EVENT_TITLE).orEmpty()
        val description = arguments?.getString(ARG_EVENT_DESCRIPTION).orEmpty()
        val date = arguments?.getString(ARG_EVENT_DATE).orEmpty()
        val location = arguments?.getString(ARG_EVENT_LOCATION).orEmpty()
        val organizer = arguments?.getString(ARG_EVENT_ORGANIZER).orEmpty()

        if (eventId.isNotEmpty()) {
            viewModel.setEvent(
                Event(
                    id = eventId,
                    title = title,
                    description = description,
                    date = date,
                    location = location,
                    organizer = organizer
                )
            )
        }

        observeEvent()
        setupBackButton()
    }

    private fun observeEvent() {
        viewModel.event.observe(viewLifecycleOwner) { event ->
            if (event != null) {
                binding.tvDetailTitle.text = event.title
                binding.tvDetailDate.text = event.date
                binding.tvDetailLocation.text = event.location
                binding.tvDetailOrganizer.text = event.organizer
                binding.tvDetailDescription.text = event.description
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
        const val ARG_EVENT_TITLE = "arg_event_title"
        const val ARG_EVENT_DESCRIPTION = "arg_event_description"
        const val ARG_EVENT_DATE = "arg_event_date"
        const val ARG_EVENT_LOCATION = "arg_event_location"
        const val ARG_EVENT_ORGANIZER = "arg_event_organizer"
    }
}
