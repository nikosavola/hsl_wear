package com.hsl.wear.ui.models

import com.hsl.wear.data.models.AutocompleteResult

/**
 * Sealed interface for all location-related callbacks in RouteInputScreen.
 * Groups related callbacks and provides type safety for location input actions.
 * The `isFrom` parameter distinguishes between "from" and "to" location actions.
 */
sealed interface LocationInputCallbacks {

    /**
     * User changed the query text in a location input field.
     * @param query The new query text
     * @param isFrom true for "from" location, false for "to" location
     */
    data class OnQueryChange(val query: String, val isFrom: Boolean) : LocationInputCallbacks

    /**
     * User requested to use current device location.
     * @param isFrom true for "from" location, false for "to" location
     */
    data class OnUseCurrentLocation(val isFrom: Boolean) : LocationInputCallbacks

    /**
     * User selected a search result from autocomplete suggestions.
     * @param result The selected autocomplete result
     * @param isFrom true for "from" location, false for "to" location
     */
    data class OnResultClick(val result: AutocompleteResult, val isFrom: Boolean) : LocationInputCallbacks

    /**
     * User cleared a selected location to allow editing.
     * @param isFrom true for "from" location, false for "to" location
     */
    data class OnClearLocation(val isFrom: Boolean) : LocationInputCallbacks

    /**
     * User requested to swap "from" and "to" locations.
     */
    object OnSwapLocations : LocationInputCallbacks

    /**
     * User requested to search for routes with current inputs.
     */
    object OnSearchRoutes : LocationInputCallbacks

    /**
     * User requested to navigate back to previous screen.
     */
    object OnNavigateBack : LocationInputCallbacks
}