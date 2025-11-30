package com.hsl.wear.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hsl.wear.data.models.RouteState
import com.hsl.wear.data.repository.TransitRepository
import com.hsl.wear.ui.models.RouteTrackingUiState
import com.hsl.wear.utils.TimeFormatter
import com.hsl.wear.utils.constants.TimeConstants
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
    private var refreshJob: kotlinx.coroutines.Job? = null

    companion object {
        private const val MIN_REFRESH_DURATION_MS = 2500L // 2.5 seconds minimum refresh duration for better UX
    }

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
                        break // Exit loop after ending navigation
                    }

                    delay(TimeConstants.REALTIME_UPDATE_INTERVAL_MS)
                } catch (e: Exception) {
                    // Continue running even if updates fail
                    android.util.Log.w("RouteTrackingViewModel", "Real-time update failed", e)
                    delay(TimeConstants.REALTIME_ERROR_RETRY_INTERVAL_MS)
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

    /**
     * Refresh real-time data for the current leg only.
     * This is called when user taps on the current leg card.
     * Has minimum 2.5s duration for better UX.
     */
    fun refreshCurrentLeg() {
        val routeState = _uiState.value.routeState ?: return

        // If already refreshing, ignore tap completely
        if (refreshJob?.isActive == true) {
            return
        }

        // Cancel any existing refresh job (shouldn't happen but just in case)
        refreshJob?.cancel()

        refreshJob = viewModelScope.launch {
            val refreshStartTime = System.currentTimeMillis()

            try {
                // Set loading state with descriptive message
                _uiState.value = _uiState.value.copy(
                    isRefreshing = true,
                    error = null
                )

                // Refresh real-time data using transit repository
                val result = transitRepository.refreshCurrentLegRealTimeData(routeState)

                // Calculate how long the API call took
                val apiCallDuration = System.currentTimeMillis() - refreshStartTime

                // Ensure minimum duration for better UX
                val remainingWaitTime = MIN_REFRESH_DURATION_MS - apiCallDuration

                result.onSuccess { updatedRouteState ->
                    // Update transit repository
                    transitRepository.saveRouteState(updatedRouteState)

                    // Wait minimum duration if API was too fast
                    if (remainingWaitTime > 0) {
                        delay(remainingWaitTime)
                    }

                    _uiState.value = _uiState.value.copy(
                        routeState = updatedRouteState,
                        isRefreshing = false
                    )
                }.onFailure { error ->
                    android.util.Log.e("RouteTrackingViewModel", "Real-time refresh failed", error)

                    // Even on error, show loading for minimum duration
                    if (remainingWaitTime > 0) {
                        delay(remainingWaitTime)
                    }

                    _uiState.value = _uiState.value.copy(
                        isRefreshing = false,
                        error = "Failed to get real-time data"
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("RouteTrackingViewModel", "Exception during refresh", e)

                // Ensure minimum loading time even for exceptions
                val exceptionDuration = System.currentTimeMillis() - refreshStartTime
                val remainingWaitTime = MIN_REFRESH_DURATION_MS - exceptionDuration

                if (remainingWaitTime > 0) {
                    delay(remainingWaitTime)
                }

                _uiState.value = _uiState.value.copy(
                    isRefreshing = false,
                    error = "Real-time update unavailable"
                )
            } finally {
                refreshJob = null
            }
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

                // Reset the flag after delay
                delay(TimeConstants.FEEDBACK_RESET_DELAY_MS)
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
        realtimeUpdatesJob?.cancel()
    }
}