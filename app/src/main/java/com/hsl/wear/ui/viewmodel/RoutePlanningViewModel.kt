package com.hsl.wear.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hsl.wear.data.models.AutocompleteResult
import com.hsl.wear.data.models.Location
import com.hsl.wear.data.repository.TransitRepository
import com.hsl.wear.location.LocationProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RoutePlanningUiState(
    val fromQuery: String = "",
    val toQuery: String = "",
    val fromSearchResults: List<AutocompleteResult> = emptyList(),
    val toSearchResults: List<AutocompleteResult> = emptyList(),
    val selectedFromLocation: Location? = null,
    val selectedToLocation: Location? = null,
    val routes: List<com.hsl.wear.data.models.Itinerary> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingRoutes: Boolean = false,
    val error: String? = null,
    val shouldNavigateToFromResults: Boolean = false,
    val shouldNavigateToToResults: Boolean = false
)

@HiltViewModel
class RoutePlanningViewModel @Inject constructor(
    private val transitRepository: TransitRepository,
    private val locationProvider: LocationProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoutePlanningUiState())
    val uiState: StateFlow<RoutePlanningUiState> = _uiState.asStateFlow()

    fun updateFromQuery(query: String) {
        _uiState.value = _uiState.value.copy(fromQuery = query)
    }

    fun updateToQuery(query: String) {
        _uiState.value = _uiState.value.copy(toQuery = query)
    }

    fun searchFromLocations() {
        val query = _uiState.value.fromQuery
        if (query.isBlank()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            transitRepository.autocompleteLocations(query)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        fromSearchResults = it,
                        isLoading = false,
                        shouldNavigateToFromResults = true
                    )
                }.onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to search locations: ${it.message}"
                    )
                }
        }
    }

    fun searchToLocations() {
        val query = _uiState.value.toQuery
        if (query.isBlank()) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            transitRepository.autocompleteLocations(query)
                .onSuccess {
                    _uiState.value = _uiState.value.copy(
                        toSearchResults = it,
                        isLoading = false,
                        shouldNavigateToToResults = true
                    )
                }.onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to search locations: ${it.message}"
                    )
                }
        }
    }

    fun selectFromLocation(result: AutocompleteResult) {
        val location = transitRepository.createStopLocation(result)
        _uiState.value = _uiState.value.copy(
            selectedFromLocation = location,
            fromSearchResults = emptyList()
        )
    }

    fun selectToLocation(result: AutocompleteResult) {
        val location = transitRepository.createStopLocation(result)
        _uiState.value = _uiState.value.copy(
            selectedToLocation = location,
            toSearchResults = emptyList()
        )
        // Automatically trigger route planning when both locations are selected
        planRoutes()
    }

    fun setFromLocation(location: Location) {
        _uiState.value = _uiState.value.copy(
            selectedFromLocation = location
        )
    }

    fun setToLocation(location: Location) {
        _uiState.value = _uiState.value.copy(
            selectedToLocation = location
        )
        // Automatically trigger route planning when both locations are selected
        planRoutes()
    }

    fun planRoutes() {
        val from = _uiState.value.selectedFromLocation
        val to = _uiState.value.selectedToLocation

        if (from == null || to == null) {
            _uiState.value = _uiState.value.copy(
                error = "Please select both from and to locations"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingRoutes = true, error = null)
            transitRepository.planRoute(from, to)
                .onSuccess { routes ->
                    _uiState.value = _uiState.value.copy(
                        routes = routes,
                        isLoadingRoutes = false
                    )
                }.onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoadingRoutes = false,
                        error = "Failed to plan route: ${error.message}"
                    )
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearFromSearchResults() {
        _uiState.value = _uiState.value.copy(fromSearchResults = emptyList())
    }

    fun clearToSearchResults() {
        _uiState.value = _uiState.value.copy(toSearchResults = emptyList())
    }

    fun consumeFromResultsNavigation() {
        _uiState.value = _uiState.value.copy(shouldNavigateToFromResults = false)
    }

    fun consumeToResultsNavigation() {
        _uiState.value = _uiState.value.copy(shouldNavigateToToResults = false)
    }

    fun reset() {
        _uiState.value = RoutePlanningUiState()
    }

/**
     * Checks location permission and updates state if not granted.
     * @return true if permission is granted, false otherwise
     */
    private fun checkLocationPermission(): Boolean {
        return if (locationProvider.hasLocationPermission()) {
            android.util.Log.d("RoutePlanningViewModel", "Permission granted, getting location...")
            true
        } else {
            android.util.Log.e("RoutePlanningViewModel", "Location permission not granted")
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                error = "Location permission not granted. Please enable location in settings."
            )
            false
        }
    }

    /**
     * Gets current location from location provider.
     * @return Android location or null if unavailable
     */
    private suspend fun getCurrentLocation(): android.location.Location? {
        val location = locationProvider.getCurrentLocation()
        return if (location != null) {
            android.util.Log.d("RoutePlanningViewModel", "Got GPS location: ${location.latitude}, ${location.longitude}")
            location
        } else {
            android.util.Log.e("RoutePlanningViewModel", "Location is null")
            null
        }
    }

    /**
     * Updates UI state with error when location is unavailable.
     */
    private fun handleLocationUnavailable() {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            error = "Could not get current location. Make sure location is enabled."
        )
    }

    /**
     * Updates UI state with search results from reverse geocoding.
     */
    private fun handleGeocodingSuccess(results: List<AutocompleteResult>, isForFromLocation: Boolean) {
        android.util.Log.d("RoutePlanningViewModel", "Got ${results.size} nearby locations")
        
        if (isForFromLocation) {
            _uiState.value = _uiState.value.copy(
                fromSearchResults = results,
                isLoading = false,
                shouldNavigateToFromResults = true
            )
        } else {
            _uiState.value = _uiState.value.copy(
                toSearchResults = results,
                isLoading = false,
                shouldNavigateToToResults = true
            )
        }
    }

    /**
     * Updates UI state with reverse geocoding error.
     */
    private fun handleGeocodingFailure(error: Throwable) {
        android.util.Log.e("RoutePlanningViewModel", "Reverse geocoding failed: ${error.message}")
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            error = "Could not find address for current location: ${error.message}"
        )
    }

    fun useCurrentLocation(isForFromLocation: Boolean = true) {
        viewModelScope.launch {
            android.util.Log.d("RoutePlanningViewModel", "useCurrentLocation called for ${if (isForFromLocation) "from" else "to"}")
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            // Check location permission
            if (!checkLocationPermission()) {
                return@launch
            }

            // Get current location
            val androidLocation = getCurrentLocation()
            if (androidLocation == null) {
                handleLocationUnavailable()
                return@launch
            }

            // Reverse geocode to get nearby location options
            val result = transitRepository.reverseGeocodeLocation(androidLocation.latitude, androidLocation.longitude)
            result.onSuccess { results ->
                handleGeocodingSuccess(results, isForFromLocation)
            }.onFailure { error ->
                handleGeocodingFailure(error)
            }
        }
    }
}
