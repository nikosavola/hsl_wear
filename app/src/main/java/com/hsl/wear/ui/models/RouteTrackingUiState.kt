package com.hsl.wear.ui.models

import com.hsl.wear.data.models.RouteState
import com.hsl.wear.data.models.NotificationState
import com.hsl.wear.data.models.NotificationPreferences

data class RouteTrackingUiState(
    val routeState: RouteState? = null,
    val currentTime: Long = System.currentTimeMillis(),
    val isLoading: Boolean = true,
    val hasActiveRoute: Boolean = false,
    val error: String? = null,
    val navigationEnded: Boolean = false,
    val routeSaved: Boolean = false,
    val notificationState: NotificationState = NotificationState(),
    val notificationPreferences: NotificationPreferences? = null,
    val isApproachingTransfer: Boolean = false,
    val isApproachingDestination: Boolean = false
)