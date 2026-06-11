package com.mdp.caremate.ui.community

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.mdp.caremate.R
import com.mdp.caremate.databinding.FragmentCommunityBinding

class CommunityFragment : Fragment() {

    private var _binding: FragmentCommunityBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CommunityViewModel by viewModels()
    private lateinit var eventAdapter: EventAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCommunityBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupEventList()
        observeEvents()
    }

    private fun setupEventList() {
        eventAdapter = EventAdapter { event ->
            // Kirim event ID ke EventDetailFragment saat item diklik
            val args = Bundle().apply {
                putString(EventDetailFragment.ARG_EVENT_ID, event.eid)
                putString(EventDetailFragment.ARG_EVENT_TITLE, event.title)
                putString(EventDetailFragment.ARG_EVENT_DESCRIPTION, event.description)
                putString(EventDetailFragment.ARG_EVENT_DATE, event.date)
                putString(EventDetailFragment.ARG_EVENT_LOCATION, event.location)
                putString(EventDetailFragment.ARG_EVENT_ORGANIZER, event.organizer)
            }
            findNavController().navigate(R.id.action_dest_community_to_dest_event_detail, args)
        }
        binding.rvEvents.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = eventAdapter
        }
    }

    private fun observeEvents() {
        viewModel.events.observe(viewLifecycleOwner) { events ->
            eventAdapter.submitList(events)
            binding.rvEvents.isVisible = events.isNotEmpty()
            binding.tvCommunityEmpty.isVisible = events.isEmpty()
        }
    }

    override fun onDestroyView() {
        binding.rvEvents.adapter = null
        _binding = null
        super.onDestroyView()
    }
}
