package com.hsl.wear.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hsl.wear.data.models.AutocompleteResult
import com.hsl.wear.data.models.Location
import com.hsl.wear.data.repository.TransitRepository
import com.hsl.wear.location.LocationProvider
import com.hsl.wear.ui.location.LocationRanker
import com.hsl.wear.ui.location.LocationContext
import com.hsl.wear.utils.constants.TimeConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RouteInputUiState(
    val fromQuery: String = "",
    val toQuery: String = "",
    val selectedFromLocation: Location? = null,
    val selectedToLocation: Location? = null,
    val fromSearchResults: List<AutocompleteResult> = emptyList(),
    val toSearchResults: List<AutocompleteResult> = emptyList(),
    val isLoadingFromLocation: Boolean = false,
    val isLoadingToLocation: Boolean = false,
    val isSearchingFrom: Boolean = false,
    val isSearchingTo: Boolean = false,
    val error: String? = null,
    val canSearchRoutes: Boolean = false
)

@HiltViewModel
class RouteInputViewModel @Inject constructor(
    private val transitRepository: TransitRepository,
    private val locationProvider: LocationProvider,
    private val locationRanker: LocationRanker
) : ViewModel() {

    private val _uiState = MutableStateFlow(RouteInputUiState())
    val uiState: StateFlow<RouteInputUiState> = _uiState.asStateFlow()

    private var fromSearchJob: Job? = null
    private var toSearchJob: Job? = null

    init {
        // Update canSearchRoutes whenever relevant state changes
        updateCanSearchRoutes()
    }

    fun updateFromQuery(query: String) {
        _uiState.value = _uiState.value.copy(fromQuery = query)

        // Debounced search
        fromSearchJob?.cancel()
        if (query.isNotBlank()) {
            fromSearchJob = viewModelScope.launch {
                delay(TimeConstants.BUTTON_DEBOUNCE_MS)
                searchFromLocations(query)
            }
        } else {
            _uiState.value = _uiState.value.copy(fromSearchResults = emptyList())
        }
        updateCanSearchRoutes()
    }

    fun updateToQuery(query: String) {
        _uiState.value = _uiState.value.copy(toQuery = query)

        // Debounced search
        toSearchJob?.cancel()
        if (query.isNotBlank()) {
            toSearchJob = viewModelScope.launch {
                delay(TimeConstants.BUTTON_DEBOUNCE_MS)
                searchToLocations(query)
            }
        } else {
            _uiState.value = _uiState.value.copy(toSearchResults = emptyList())
        }
        updateCanSearchRoutes()
    }

    private fun searchFromLocations(query: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearchingFrom = true, error = null)
            transitRepository.autocompleteLocations(query)
                .onSuccess { results ->
                    // Apply smart ranking
                    val context = LocationContext(
                        currentLocation = getCurrentLocation(),
                        recentLocations = transitRepository.recentLocations.first(),
                        favoriteLocations = transitRepository.favoriteLocations.first(),
                        currentTime = System.currentTimeMillis()
                    )

                    val rankedResults = locationRanker.getTopResults(results, query, context)

                    _uiState.value = _uiState.value.copy(
                        fromSearchResults = rankedResults,
                        isSearchingFrom = false
                    )
                }.onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isSearchingFrom = false,
                        error = "Failed to search from locations: ${error.message}"
                    )
                }
        }
    }

    private fun searchToLocations(query: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearchingTo = true, error = null)
            transitRepository.autocompleteLocations(query)
                .onSuccess { results ->
                    // Apply smart ranking
                    val context = LocationContext(
                        currentLocation = getCurrentLocation(),
                        recentLocations = transitRepository.recentLocations.first(),
                        favoriteLocations = transitRepository.favoriteLocations.first(),
                        currentTime = System.currentTimeMillis()
                    )

                    val rankedResults = locationRanker.getTopResults(results, query, context)

                    _uiState.value = _uiState.value.copy(
                        toSearchResults = rankedResults,
                        isSearchingTo = false
                    )
                }.onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isSearchingTo = false,
                        error = "Failed to search to locations: ${error.message}"
                    )
                }
        }
    }

    fun selectFromLocation(result: AutocompleteResult) {
        val location = transitRepository.createStopLocation(result)

        // Track usage for smart ranking
        viewModelScope.launch {
            transitRepository.addRecentLocation(location)
        }

        _uiState.value = _uiState.value.copy(
            selectedFromLocation = location,
            fromSearchResults = emptyList(),
            fromQuery = "" // Clear query after selection
        )
        updateCanSearchRoutes()
    }

    fun selectToLocation(result: AutocompleteResult) {
        val location = transitRepository.createStopLocation(result)

        // Track usage for smart ranking
        viewModelScope.launch {
            transitRepository.addRecentLocation(location)
        }

        _uiState.value = _uiState.value.copy(
            selectedToLocation = location,
            toSearchResults = emptyList(),
            toQuery = "" // Clear query after selection
        )
        updateCanSearchRoutes()
    }

    fun useCurrentLocationForFrom() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingFromLocation = true, error = null)

            if (!locationProvider.hasLocationPermission()) {
                _uiState.value = _uiState.value.copy(
                    isLoadingFromLocation = false,
                    error = "Location permission not granted. Please enable location in settings."
                )
                return@launch
            }

            val androidLocation = locationProvider.getCurrentLocation()
            if (androidLocation == null) {
                _uiState.value = _uiState.value.copy(
                    isLoadingFromLocation = false,
                    error = "Could not get current location. Make sure location is enabled."
                )
                return@launch
            }

            val result = transitRepository.reverseGeocodeLocation(
                androidLocation.latitude,
                androidLocation.longitude
            )

            result.onSuccess { results ->
                _uiState.value = _uiState.value.copy(
                    fromSearchResults = results,
                    isLoadingFromLocation = false
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoadingFromLocation = false,
                    error = "Could not find address for current location: ${error.message}"
                )
            }
        }
    }

    fun useCurrentLocationForTo() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingToLocation = true, error = null)

            if (!locationProvider.hasLocationPermission()) {
                _uiState.value = _uiState.value.copy(
                    isLoadingToLocation = false,
                    error = "Location permission not granted. Please enable location in settings."
                )
                return@launch
            }

            val androidLocation = locationProvider.getCurrentLocation()
            if (androidLocation == null) {
                _uiState.value = _uiState.value.copy(
                    isLoadingToLocation = false,
                    error = "Could not get current location. Make sure location is enabled."
                )
                return@launch
            }

            val result = transitRepository.reverseGeocodeLocation(
                androidLocation.latitude,
                androidLocation.longitude
            )

            result.onSuccess { results ->
                _uiState.value = _uiState.value.copy(
                    toSearchResults = results,
                    isLoadingToLocation = false
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoadingToLocation = false,
                    error = "Could not find address for current location: ${error.message}"
                )
            }
        }
    }

    fun swapLocations() {
        try {
            val currentState = _uiState.value

            // Only swap selected locations and queries, clear search results to avoid conflicts
            _uiState.value = currentState.copy(
                selectedFromLocation = currentState.selectedToLocation,
                selectedToLocation = currentState.selectedFromLocation,
                fromQuery = currentState.toQuery ?: "",
                toQuery = currentState.fromQuery ?: "",
                fromSearchResults = emptyList(),
                toSearchResults = emptyList()
            )
            updateCanSearchRoutes()
        } catch (e: Exception) {
            // Log the error for debugging and reset to safe state
            android.util.Log.e("RouteInputViewModel", "Error in swapLocations: ${e.message}", e)
            _uiState.value = RouteInputUiState()
        }
    }

    fun clearFromLocation() {
        _uiState.value = _uiState.value.copy(
            selectedFromLocation = null,
            fromQuery = "",
            fromSearchResults = emptyList()
        )
        updateCanSearchRoutes()
    }

    fun clearToLocation() {
        _uiState.value = _uiState.value.copy(
            selectedToLocation = null,
            toQuery = "",
            toSearchResults = emptyList()
        )
        updateCanSearchRoutes()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun updateCanSearchRoutes() {
        val state = _uiState.value
        val canSearch = (state.selectedFromLocation != null || state.fromQuery.isNotBlank()) &&
                       (state.selectedToLocation != null || state.toQuery.isNotBlank()) &&
                       !state.isLoadingFromLocation &&
                       !state.isLoadingToLocation &&
                       !state.isSearchingFrom &&
                       !state.isSearchingTo

        if (state.canSearchRoutes != canSearch) {
            _uiState.value = state.copy(canSearchRoutes = canSearch)
        }
    }

    fun getLocationsForRoutePlanning(): Pair<Location?, Location?> {
        val from = _uiState.value.selectedFromLocation
        val to = _uiState.value.selectedToLocation

        // Only use properly selected locations for route planning
        // Query-based locations require geocoding before they can be used
        return Pair(from, to)
    }

    private suspend fun getCurrentLocation(): Location? {
        val androidLocation = locationProvider.getCurrentLocation() ?: return null
        return Location(
            id = "current_location",
            name = "Current Location",
            lat = androidLocation.latitude,
            lon = androidLocation.longitude,
            type = com.hsl.wear.data.models.LocationType.CURRENT_LOCATION
        )
    }

    fun reset() {
        _uiState.value = RouteInputUiState()
    }

    override fun onCleared() {
        super.onCleared()
        fromSearchJob?.cancel()
        toSearchJob?.cancel()
    }
}