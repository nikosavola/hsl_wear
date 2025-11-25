package com.hsl.wear.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hsl.wear.data.models.Itinerary
import com.hsl.wear.data.models.RouteState
import com.hsl.wear.data.repository.TransitRepository
import com.hsl.wear.ui.models.RouteSelectionUiState
import com.hsl.wear.utils.TimeFormatter
import com.hsl.wear.utils.DistanceFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RouteSelectionViewModel @Inject constructor(
    private val transitRepository: TransitRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RouteSelectionUiState())
    val uiState: StateFlow<RouteSelectionUiState> = _uiState.asStateFlow()

    fun loadRoutes(routes: List<Itinerary>, isLoading: Boolean = false) {
        _uiState.value = _uiState.value.copy(
            availableRoutes = routes,
            isLoading = isLoading,
            error = null
        )
    }

    fun selectRoute(
        itinerary: Itinerary,
        fromLocation: com.hsl.wear.data.models.Location? = null,
        toLocation: com.hsl.wear.data.models.Location? = null
    ) {
        _uiState.value = _uiState.value.copy(
            selectedRoute = itinerary,
            isLoading = true,
            error = null
        )

        viewModelScope.launch {
            try {
                val routeState = RouteState(
                    itineraryId = itinerary.id,
                    legs = itinerary.legs,
                    startTimeIso = itinerary.startTimeIso,
                    fromLocation = fromLocation,
                    toLocation = toLocation
                )

                transitRepository.saveRouteState(routeState)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = null,
                    navigationStarted = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun formatStartTime(isoTimestamp: String): String = TimeFormatter.formatTime(isoTimestamp)
    fun formatDuration(minutes: Int): String = TimeFormatter.formatDuration(minutes)
    fun formatLegDuration(seconds: Int): String = TimeFormatter.formatLegDuration(seconds)
    fun formatLegDistance(meters: Int): String = DistanceFormatter.formatDistance(meters)
    fun formatWalkingDistance(meters: Int): String = DistanceFormatter.formatWalkingDistance(meters)

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun resetNavigationState() {
        _uiState.value = _uiState.value.copy(navigationStarted = false)
    }
}