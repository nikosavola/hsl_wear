package com.hsl.wear.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hsl.wear.data.models.RouteState
import com.hsl.wear.data.repository.TransitRepository
import com.hsl.wear.ui.models.RouteTrackingUiState
import com.hsl.wear.utils.TimeFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RouteTrackingViewModel @Inject constructor(
    private val transitRepository: TransitRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RouteTrackingUiState())
    val uiState: StateFlow<RouteTrackingUiState> = _uiState.asStateFlow()

    private var realtimeUpdatesJob: kotlinx.coroutines.Job? = null

    init {
        viewModelScope.launch {
            loadActiveRoute()
        }
    }

    private suspend fun loadActiveRoute() {
        try {
            val activeRoute = transitRepository.activeRouteState.first()
            if (activeRoute != null) {
                _uiState.value = _uiState.value.copy(
                    routeState = activeRoute,
                    isLoading = false,
                    error = null,
                    hasActiveRoute = true
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    hasActiveRoute = false
                )
            }
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = e.message,
                hasActiveRoute = false
            )
        }
    }

    /**
     * Start realtime updates - should be called when screen becomes visible
     */
    fun startRealtimeUpdates() {
        // Cancel any existing job
        realtimeUpdatesJob?.cancel()

        realtimeUpdatesJob = viewModelScope.launch {
            while (isActive) {
                try {
                    val currentTime = System.currentTimeMillis()
                    val routeState = _uiState.value.routeState

                    // Update current time
                    _uiState.value = _uiState.value.copy(currentTime = currentTime)

                    // Note: Pre-arrival notifications are now handled by AlarmManager
                    // for battery efficiency. The system wakes the app at exact times.

                    // Auto-end route tracking 2 minutes after final leg arrival
                    // This works regardless of which leg the user is currently viewing
                    if (routeState != null && isRouteObsolete(routeState, currentTime)) {
                        endNavigation()
                    }

                    delay(30000) // Update every 30 seconds
                } catch (e: Exception) {
                    // Continue running even if updates fail
                    delay(60000) // Wait longer if there's an error
                }
            }
        }
    }

    /**
     * Stop realtime updates - should be called when screen is no longer visible
     */
    fun stopRealtimeUpdates() {
        realtimeUpdatesJob?.cancel()
        realtimeUpdatesJob = null
    }

    fun moveToNextLeg() {
        val currentState = _uiState.value.routeState
        if (currentState == null || currentState.isComplete) {
            return
        }

        viewModelScope.launch {
            try {
                val result = transitRepository.advanceToNextLeg()
                result.onSuccess { newState ->
                    _uiState.value = _uiState.value.copy(
                        routeState = newState
                    )

                }.onFailure { error ->
                    _uiState.value = _uiState.value.copy(error = error.message)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun moveToPreviousLeg() {
        val currentState = _uiState.value.routeState
        if (currentState == null || currentState.currentIndex <= 0) {
            return
        }

        viewModelScope.launch {
            try {
                val result = transitRepository.moveToPreviousLeg()
                result.onSuccess { newState ->
                    _uiState.value = _uiState.value.copy(
                        routeState = newState
                    )
                }.onFailure { error ->
                    _uiState.value = _uiState.value.copy(error = error.message)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun jumpToLeg(legIndex: Int) {
        viewModelScope.launch {
            try {
                val result = transitRepository.jumpToLeg(legIndex)
                result.onSuccess { newState ->
                    _uiState.value = _uiState.value.copy(
                        routeState = newState
                    )
                }.onFailure { error ->
                    _uiState.value = _uiState.value.copy(error = error.message)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun endNavigation() {
        viewModelScope.launch {
            try {
                transitRepository.clearRouteState()
                _uiState.value = _uiState.value.copy(
                    navigationEnded = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun getCurrentLegStatus(): String? {
        val currentLeg = _uiState.value.routeState?.currentLeg
        val currentTime = _uiState.value.currentTime

        return currentLeg?.getStatusText(currentTime)
    }

    fun getCurrentLegTransportInfo(): Pair<String, String> {
        val currentLeg = _uiState.value.routeState?.currentLeg ?: return Pair("", "")

        val transportType = when (currentLeg.mode) {
            "BUS" -> "Bus"
            "TRAM" -> "Tram"
            "RAIL" -> "Train"
            "SUBWAY" -> "Metro"
            "FERRY" -> "Ferry"
            "WALK" -> "Walk"
            else -> currentLeg.mode
        }

        val transportName = currentLeg.line ?: transportType

        return Pair(transportType, transportName)
    }

    fun refreshRoute() {
        // Check if route is obsolete before refreshing
        val routeState = _uiState.value.routeState
        val currentTime = System.currentTimeMillis()

        if (routeState != null && isRouteObsolete(routeState, currentTime)) {
            endNavigation()
            return
        }

        viewModelScope.launch {
            loadActiveRoute()
        }
    }

    private fun isRouteObsolete(routeState: RouteState, currentTime: Long): Boolean {
        val lastLeg = routeState.legs.lastOrNull() ?: return false

        // Calculate final arrival time (last leg's start time + duration)
        val startTime = TimeFormatter.parseIsoTime(lastLeg.realtimeTimeIso ?: lastLeg.scheduledTimeIso)
        val finalArrivalTime = startTime + (lastLeg.duration * 1000)
        val autoEndTime = finalArrivalTime + (2 * 60 * 1000) // 2 minutes after arrival

        return currentTime >= autoEndTime
    }

    fun saveRouteAsFavorite() {
        val routeState = _uiState.value.routeState
        if (routeState?.fromLocation == null || routeState.toLocation == null) {
            _uiState.value = _uiState.value.copy(error = "Cannot save route: missing location information")
            return
        }

        viewModelScope.launch {
            try {
                val favoriteRoute = com.hsl.wear.data.models.FavoriteRoute(
                    id = "${routeState.fromLocation.id}_${routeState.toLocation.id}_${System.currentTimeMillis()}",
                    name = "${routeState.fromLocation.name} → ${routeState.toLocation.name}",
                    fromLocation = routeState.fromLocation,
                    toLocation = routeState.toLocation
                )
                transitRepository.addFavoriteRoute(favoriteRoute)

                // Show success feedback
                _uiState.value = _uiState.value.copy(routeSaved = true)

                // Reset the flag after 2 seconds
                delay(2000)
                _uiState.value = _uiState.value.copy(routeSaved = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Failed to save favorite: ${e.message}")
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    override fun onCleared() {
        super.onCleared()
        // Cleanup coroutines will be handled automatically
    }
}