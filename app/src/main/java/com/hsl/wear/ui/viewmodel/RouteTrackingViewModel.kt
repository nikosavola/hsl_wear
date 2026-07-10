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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RouteTrackingViewModel @Inject constructor(
    private val transitRepository: TransitRepository
) : ViewModel() {

    private val _localState = MutableStateFlow(RouteTrackingUiState(isLoading = false))

    val uiState: StateFlow<RouteTrackingUiState> = combine(
        transitRepository.activeRouteState,
        _localState
    ) { activeRoute, localState ->
        localState.copy(
            routeState = activeRoute,
            hasActiveRoute = activeRoute != null
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = RouteTrackingUiState(isLoading = true)
    )

    private var realtimeUpdatesJob: kotlinx.coroutines.Job? = null
    private var refreshJob: kotlinx.coroutines.Job? = null

    companion object {
        private const val MIN_REFRESH_DURATION_MS = 2500L
    }

    init {
        // Handled by flows now
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
                    val routeState = uiState.value.routeState

                    // Update current time
                    _localState.update { it.copy(currentTime = currentTime) }

                    // Auto-end route tracking 2 minutes after final leg arrival
                    if (routeState != null && isRouteObsolete(routeState, currentTime)) {
                        endNavigation()
                        break // Exit loop after ending navigation
                    }

                    delay(TimeConstants.REALTIME_UPDATE_INTERVAL_MS)
                } catch (e: Exception) {
                    android.util.Log.w("RouteTrackingViewModel", "Real-time update failed", e)
                    delay(TimeConstants.REALTIME_ERROR_RETRY_INTERVAL_MS)
                }
            }
        }
    }

    fun stopRealtimeUpdates() {
        realtimeUpdatesJob?.cancel()
        realtimeUpdatesJob = null
    }

    fun moveToNextLeg() {
        val currentState = uiState.value.routeState
        if (currentState == null || currentState.isComplete) {
            return
        }

        viewModelScope.launch {
            try {
                val result = transitRepository.advanceToNextLeg()
                result.onFailure { error ->
                    _localState.update { it.copy(error = error.message) }
                }
            } catch (e: Exception) {
                _localState.update { it.copy(error = e.message) }
            }
        }
    }

    fun moveToPreviousLeg() {
        val currentState = uiState.value.routeState
        if (currentState == null || currentState.currentIndex <= 0) {
            return
        }

        viewModelScope.launch {
            try {
                val result = transitRepository.moveToPreviousLeg()
                result.onFailure { error ->
                    _localState.update { it.copy(error = error.message) }
                }
            } catch (e: Exception) {
                _localState.update { it.copy(error = e.message) }
            }
        }
    }

    fun jumpToLeg(legIndex: Int) {
        viewModelScope.launch {
            try {
                val result = transitRepository.jumpToLeg(legIndex)
                result.onFailure { error ->
                    _localState.update { it.copy(error = error.message) }
                }
            } catch (e: Exception) {
                _localState.update { it.copy(error = e.message) }
            }
        }
    }

    fun endNavigation() {
        viewModelScope.launch {
            try {
                transitRepository.clearRouteState()
                _localState.update { it.copy(navigationEnded = true) }
            } catch (e: Exception) {
                _localState.update { it.copy(error = e.message) }
            }
        }
    }

    fun getCurrentLegStatus(): String? {
        val currentLeg = uiState.value.routeState?.currentLeg
        val currentTime = uiState.value.currentTime

        return currentLeg?.getStatusText(currentTime)
    }

    fun getCurrentLegTransportInfo(): Pair<String, String> {
        val currentLeg = uiState.value.routeState?.currentLeg ?: return Pair("", "")

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


    /**
     * Refresh real-time data for the current leg only.
     */
    fun refreshCurrentLeg() {
        val routeState = uiState.value.routeState ?: return

        // If already refreshing, ignore tap completely
        if (refreshJob?.isActive == true) {
            return
        }

        refreshJob?.cancel()

        refreshJob = viewModelScope.launch {
            val refreshStartTime = System.currentTimeMillis()

            try {
                _localState.update { it.copy(isRefreshing = true, error = null) }

                val result = transitRepository.refreshCurrentLegRealTimeData(routeState)

                val apiCallDuration = System.currentTimeMillis() - refreshStartTime
                val remainingWaitTime = MIN_REFRESH_DURATION_MS - apiCallDuration

                result.onSuccess { updatedRouteState ->
                    transitRepository.saveRouteState(updatedRouteState)

                    if (remainingWaitTime > 0) {
                        delay(remainingWaitTime)
                    }

                    _localState.update { it.copy(isRefreshing = false) }
                }.onFailure { error ->
                    android.util.Log.e("RouteTrackingViewModel", "Real-time refresh failed", error)

                    if (remainingWaitTime > 0) {
                        delay(remainingWaitTime)
                    }

                    _localState.update { it.copy(isRefreshing = false, error = "Failed to get real-time data") }
                }
            } catch (e: Exception) {
                android.util.Log.e("RouteTrackingViewModel", "Exception during refresh", e)

                val exceptionDuration = System.currentTimeMillis() - refreshStartTime
                val remainingWaitTime = MIN_REFRESH_DURATION_MS - exceptionDuration

                if (remainingWaitTime > 0) {
                    delay(remainingWaitTime)
                }

                _localState.update { it.copy(isRefreshing = false, error = "Real-time update unavailable") }
            } finally {
                refreshJob = null
            }
        }
    }

    private fun isRouteObsolete(routeState: RouteState, currentTime: Long): Boolean {
        val lastLeg = routeState.legs.lastOrNull() ?: return false
        val startTime = TimeFormatter.parseIsoTime(lastLeg.realtimeTimeIso ?: lastLeg.scheduledTimeIso)
        val finalArrivalTime = startTime + (lastLeg.duration * 1000)
        val autoEndTime = finalArrivalTime + (2 * 60 * 1000)
        return currentTime >= autoEndTime
    }

    fun saveRouteAsFavorite() {
        val routeState = uiState.value.routeState
        if (routeState?.fromLocation == null || routeState.toLocation == null) {
            _localState.update { it.copy(error = "Cannot save route: missing location information") }
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

                _localState.update { it.copy(routeSaved = true) }

                delay(TimeConstants.FEEDBACK_RESET_DELAY_MS)
                _localState.update { it.copy(routeSaved = false) }
            } catch (e: Exception) {
                _localState.update { it.copy(error = "Failed to save favorite: ${e.message}") }
            }
        }
    }

    fun clearError() {
        _localState.update { it.copy(error = null) }
    }

    override fun onCleared() {
        super.onCleared()
        realtimeUpdatesJob?.cancel()
    }
}
