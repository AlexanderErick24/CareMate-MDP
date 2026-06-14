package com.mdp.caremate.ui.admin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mdp.caremate.data.model.Event

data class DashboardMetricsState(
    val totalEvents: Int = 0,
    val totalCapacitySlots: Int = 0,
    val unlistedEventsCount: Int = 0,
    val capacityTrendText: String = ""
)

class DashboardViewModel : ViewModel() {

    private val _metricsState = MutableLiveData<DashboardMetricsState>()
    val metricsState: LiveData<DashboardMetricsState> get() = _metricsState

    /**
     * Call this function when you fetch your event collection from Firebase,
     * Room Database, or an API client.
     */
    fun processEventData(eventsList: List<Event>) {
        val totalEvents = eventsList.size

        // Sum up all slots safely converting String to Integer
        val totalCapacity = eventsList.sumOf { event ->
            event.capacity.toIntOrNull() ?: 0
        }

        // Filter elements where listed == false
        val unlistedCount = eventsList.count { !it.listed }

        _metricsState.value = DashboardMetricsState(
            totalEvents = totalEvents,
            totalCapacitySlots = totalCapacity,
            unlistedEventsCount = unlistedCount,
            capacityTrendText = "👥 Avg: ${if (totalEvents > 0) totalCapacity / totalEvents else 0} slots per event"
        )
    }
}