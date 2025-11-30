package com.hsl.wear.ui.models

import com.hsl.wear.data.models.Location
import com.hsl.wear.data.models.AutocompleteResult

/**
 * Groups all state related to a location input (either "from" or "to" location)
 * to reduce parameter count and improve type safety in RouteInputScreen.
 */
data class LocationInputState(
    val query: String = "",
    val selectedLocation: Location? = null,
    val searchResults: List<AutocompleteResult> = emptyList(),
    val isLoading: Boolean = false,
    val title: String,
    val placeholder: String
)