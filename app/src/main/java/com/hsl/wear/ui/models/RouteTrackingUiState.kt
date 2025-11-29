package com.hsl.wear.ui.models

import com.hsl.wear.data.models.RouteState

data class RouteTrackingUiState(
    val routeState: RouteState? = null,
    val currentTime: Long = System.currentTimeMillis(),
    val isLoading: Boolean = true,
    val hasActiveRoute: Boolean = false,
    val error: String? = null,
    val navigationEnded: Boolean = false,
    val routeSaved: Boolean = false,
    val isRefreshing: Boolean = false
)