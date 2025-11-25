package com.hsl.wear.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hsl.wear.data.models.RouteState
import com.hsl.wear.data.models.NotificationPreferences
import com.hsl.wear.data.models.NotificationState
import com.hsl.wear.data.models.NotificationType
import com.hsl.wear.data.models.VibrationIntensity
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

    init {
        loadActiveRoute()
        loadNotificationPreferences()
        startRealtimeUpdates()
    }

    private fun loadActiveRoute() {
        viewModelScope.launch {
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
    }

    private fun loadNotificationPreferences() {
        viewModelScope.launch {
            try {
                val preferences = transitRepository.notificationPreferencesFlow.first()
                _uiState.value = _uiState.value.copy(
                    notificationPreferences = preferences
                )
            } catch (e: Exception) {
                // Use default preferences if loading fails
                _uiState.value = _uiState.value.copy(
                    notificationPreferences = NotificationPreferences()
                )
            }
        }
    }

    private fun startRealtimeUpdates() {
        viewModelScope.launch {
            while (isActive) {
                try {
                    val currentTime = System.currentTimeMillis()
                    val routeState = _uiState.value.routeState

                    // Update current time
                    _uiState.value = _uiState.value.copy(currentTime = currentTime)

                    // Check for pre-arrival notifications if route is active
                    if (routeState != null && _uiState.value.notificationPreferences?.preArrivalEnabled == true) {
                        checkPreArrivalNotifications(routeState, currentTime)
                    }

                    delay(30000) // Update every 30 seconds
                } catch (e: Exception) {
                    // Continue running even if updates fail
                    delay(60000) // Wait longer if there's an error
                }
            }
        }
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
        loadActiveRoute()
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

    private fun checkPreArrivalNotifications(routeState: RouteState, currentTime: Long) {
        val preferences = _uiState.value.notificationPreferences ?: return
        val notificationState = _uiState.value.notificationState
        val currentLeg = routeState.currentLeg

        // Don't send notifications during walking phases unless explicitly enabled
        if (currentLeg?.isWalking == true && !preferences.walkingNotificationsEnabled) {
            return
        }

        // Check for transfer notifications
        if (preferences.transferNotificationsEnabled) {
            checkTransferNotifications(routeState, currentTime, preferences, notificationState)
        }

        // Check for final destination notifications
        if (preferences.finalDestinationNotificationsEnabled) {
            checkDestinationNotifications(routeState, currentTime, preferences, notificationState)
        }
    }

    private fun checkTransferNotifications(
        routeState: RouteState,
        currentTime: Long,
        preferences: NotificationPreferences,
        notificationState: NotificationState
    ) {
        val nextTransferLegIndex = TimeFormatter.findNextTransferLegIndex(routeState.legs, routeState.currentIndex)

        nextTransferLegIndex?.let { transferLegIndex ->
            val transferLeg = routeState.legs[transferLegIndex]
            val transferArrivalTime = TimeFormatter.calculateLegArrivalTime(routeState.legs, transferLegIndex)

            // Check if we're within the pre-arrival window for the transfer
            if (TimeFormatter.isWithinPreArrivalWindow(transferArrivalTime, currentTime, preferences.advanceMinutes)) {
                // Check if we haven't already notified for this transfer
                val shouldNotify = notificationState.lastTransferNotificationTime == null ||
                        (currentTime - notificationState.lastTransferNotificationTime) > (preferences.advanceMinutes * 60 * 1000)

                if (shouldNotify && !TimeFormatter.isTransferPoint(routeState.legs, routeState.currentIndex)) {
                    triggerTransferNotification(transferLeg, transferLegIndex, preferences)
                    updateNotificationState(
                        notificationState.copy(
                            lastTransferNotificationTime = currentTime,
                            nextTransferLegIndex = transferLegIndex,
                            isApproachingTransfer = true
                        )
                    )
                }
            } else {
                // Clear the approaching transfer state if we're no longer within the window
                if (notificationState.isApproachingTransfer) {
                    updateNotificationState(
                        notificationState.copy(
                            isApproachingTransfer = false
                        )
                    )
                }
            }
        }
    }

    private fun checkDestinationNotifications(
        routeState: RouteState,
        currentTime: Long,
        preferences: NotificationPreferences,
        notificationState: NotificationState
    ) {
        val finalArrivalTime = TimeFormatter.calculateFinalArrivalTime(routeState)

        // Check if we're within the pre-arrival window for final destination
        if (TimeFormatter.isWithinPreArrivalWindow(finalArrivalTime, currentTime, preferences.advanceMinutes)) {
            // Check if we haven't already notified for final destination
            val shouldNotify = notificationState.lastDestinationNotificationTime == null ||
                    (currentTime - notificationState.lastDestinationNotificationTime) > (preferences.advanceMinutes * 60 * 1000)

            if (shouldNotify) {
                val lastLeg = routeState.legs.lastOrNull()
                if (lastLeg != null) {
                    triggerDestinationNotification(lastLeg, preferences)
                    updateNotificationState(
                        notificationState.copy(
                            lastDestinationNotificationTime = currentTime,
                            isApproachingDestination = true
                        )
                    )
                }
            }
        } else {
            // Clear the approaching destination state if we're no longer within the window
            if (notificationState.isApproachingDestination) {
                updateNotificationState(
                    notificationState.copy(
                        isApproachingDestination = false
                    )
                )
            }
        }
    }

    private fun triggerTransferNotification(
        leg: com.hsl.wear.data.models.Leg,
        legIndex: Int,
        preferences: NotificationPreferences
    ) {
        viewModelScope.launch {
            try {
                // TODO: This will be implemented when we create the NotificationManager
                // For now, we just update the UI state to indicate transfer approaching
                _uiState.value = _uiState.value.copy(isApproachingTransfer = true)

                // Trigger haptic feedback
                // TODO: This will be implemented when we extend HslHapticFeedback

            } catch (e: Exception) {
                // Handle notification trigger failure
            }
        }
    }

    private fun triggerDestinationNotification(
        leg: com.hsl.wear.data.models.Leg,
        preferences: NotificationPreferences
    ) {
        viewModelScope.launch {
            try {
                // TODO: This will be implemented when we create the NotificationManager
                // For now, we just update the UI state to indicate destination approaching
                _uiState.value = _uiState.value.copy(isApproachingDestination = true)

                // Trigger haptic feedback
                // TODO: This will be implemented when we extend HslHapticFeedback

            } catch (e: Exception) {
                // Handle notification trigger failure
            }
        }
    }

    private fun updateNotificationState(newState: NotificationState) {
        _uiState.value = _uiState.value.copy(notificationState = newState)
    }

    fun updateNotificationPreferences(preferences: NotificationPreferences) {
        viewModelScope.launch {
            try {
                transitRepository.saveNotificationPreferences(preferences)
                _uiState.value = _uiState.value.copy(notificationPreferences = preferences)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Failed to save notification preferences: ${e.message}")
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