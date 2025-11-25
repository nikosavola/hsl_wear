package com.hsl.wear.ui.models

import com.hsl.wear.data.models.Itinerary

data class RouteSelectionUiState(
    val availableRoutes: List<Itinerary> = emptyList(),
    val selectedRoute: Itinerary? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val navigationStarted: Boolean = false
)