package com.hsl.wear.data.repository

import com.hsl.wear.data.models.*
import com.hsl.wear.data.store.RouteStore
import com.hsl.wear.utils.LocationUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransitRepository @Inject constructor(
    private val hslRepository: HslRepository,
    private val routeStore: RouteStore
) {

    // Route Planning
    suspend fun autocompleteLocations(query: String): Result<List<AutocompleteResult>> {
        return hslRepository.geocodeSearch(query)
    }

    suspend fun reverseGeocodeLocation(lat: Double, lon: Double): Result<List<AutocompleteResult>> {
        return hslRepository.reverseGeocodeResults(lat, lon)
    }

    suspend fun planRoute(from: Location, to: Location): Result<List<Itinerary>> {
        return hslRepository.planRoute(from, to)
    }

    // Route State Management
    val activeRouteState: Flow<RouteState?> = routeStore.routeStateFlow

    suspend fun saveRouteState(routeState: RouteState) {
        routeStore.saveRouteState(routeState)
        // Note: Tiles rely on automatic refresh intervals (5 minutes) + dynamic expressions
    }

    suspend fun clearRouteState() {
        routeStore.clearRouteState()
        // Note: Tiles rely on automatic refresh intervals (5 minutes) + dynamic expressions
    }

    suspend fun advanceToNextLeg(): Result<RouteState> {
        val currentState = activeRouteState.first()
            ?: return Result.failure(IllegalStateException("No active route"))

        if (currentState.isComplete) {
            return Result.failure(IllegalStateException("Route already complete"))
        }

        val newState = currentState.moveToNextLeg()
        saveRouteState(newState)
        return Result.success(newState)
    }

    suspend fun moveToPreviousLeg(): Result<RouteState> {
        val currentState = activeRouteState.first()
            ?: return Result.failure(IllegalStateException("No active route"))

        if (currentState.currentIndex <= 0) {
            return Result.failure(IllegalStateException("Already at first leg"))
        }

        val newState = currentState.moveToPreviousLeg()
        saveRouteState(newState)
        return Result.success(newState)
    }

    suspend fun jumpToLeg(legIndex: Int): Result<RouteState> {
        val currentState = activeRouteState.first()
            ?: return Result.failure(IllegalStateException("No active route"))

        if (legIndex < 0 || legIndex >= currentState.legs.size) {
            return Result.failure(IllegalStateException("Invalid leg index"))
        }

        val newState = currentState.copy(
            currentIndex = legIndex,
            lastUpdated = System.currentTimeMillis()
        )
        saveRouteState(newState)
        return Result.success(newState)
    }

    // Favorite Locations
    val favoriteLocations: Flow<List<Location>> = routeStore.favoriteLocationsFlow

    suspend fun addFavoriteLocation(location: Location) {
        routeStore.addFavoriteLocation(location)
    }

    suspend fun removeFavoriteLocation(locationId: String) {
        routeStore.removeFavoriteLocation(locationId)
    }

    // Favorite Routes
    val favoriteRoutes: Flow<List<FavoriteRoute>> = routeStore.favoriteRoutesFlow

    suspend fun addFavoriteRoute(favoriteRoute: FavoriteRoute) {
        routeStore.addFavoriteRoute(favoriteRoute)
    }

    suspend fun removeFavoriteRoute(routeId: String) {
        routeStore.removeFavoriteRoute(routeId)
    }

    // Recent Locations
    val recentLocations: Flow<List<Location>> = routeStore.recentLocationsFlow

    suspend fun addRecentLocation(location: Location) {
        routeStore.addRecentLocation(location)
    }

    // Last Used Locations
    val lastFromLocation: Flow<Location?> = routeStore.lastFromLocationFlow
    val lastToLocation: Flow<Location?> = routeStore.lastToLocationFlow

    suspend fun saveLastFromLocation(location: Location) {
        routeStore.saveLastFromLocation(location)
    }

    suspend fun saveLastToLocation(location: Location) {
        routeStore.saveLastToLocation(location)
    }

    // Utility Functions
    suspend fun hasActiveRoute(): Boolean {
        return activeRouteState.first() != null
    }

    suspend fun getActiveRouteState(): RouteState? {
        return activeRouteState.first()
    }

    suspend fun clearAllData() {
        routeStore.clearAllData()
    }

    // Location Creation Helpers
    fun createStopLocation(autocompleteResult: AutocompleteResult): Location {
        return LocationUtils.createFromAutocompleteResult(autocompleteResult)
    }

    fun createCurrentLocation(lat: Double, lon: Double): Location {
        return LocationUtils.createCurrentLocation(lat, lon)
    }

    /**
     * Refresh real-time data for the current leg.
     * @param routeState Current route state
     * @return Result containing updated route state with real-time data or failure
     */
    suspend fun refreshCurrentLegRealTimeData(routeState: RouteState): Result<RouteState> {
        val currentLeg = routeState.currentLeg ?: return Result.failure(Exception("No current leg to refresh"))

        // If current leg is walking, advance to the next transit leg for real-time data
        if (currentLeg.isWalking) {

            // Find the next transit leg
            val nextTransitLegIndex = (routeState.currentIndex + 1).coerceAtMost(routeState.legs.size - 1)
            val nextLeg = routeState.legs.getOrNull(nextTransitLegIndex)

            if (nextLeg != null && !nextLeg.isWalking && nextLeg.tripGtfsId != null) {

                // Create a new route state with the advanced index for this refresh operation
                val advancedRouteState = routeState.copy(currentIndex = nextTransitLegIndex)
                return refreshTransitLegData(advancedRouteState)
            } else {
                return Result.success(routeState)
            }
        }

        return refreshTransitLegData(routeState)
    }

    /**
     * Refresh real-time data for a transit leg.
     * @param routeState Route state with current leg being a transit leg
     * @return Result containing updated route state with real-time data or failure
     */
    private suspend fun refreshTransitLegData(routeState: RouteState): Result<RouteState> {
        val currentLeg = routeState.currentLeg ?: return Result.failure(Exception("No current leg to refresh"))

        // Don't refresh walking legs (shouldn't happen here but just in case)
        if (currentLeg.isWalking) {
            return Result.success(routeState)
        }

        // Don't refresh if we don't have a trip ID
        if (currentLeg.tripGtfsId == null) {
            return Result.failure(Exception("Cannot refresh: missing trip ID"))
        }

        // Get real-time status for current trip
        return hslRepository.getTripStatus(currentLeg.tripGtfsId).map { tripStatus ->
            val updatedLeg = com.hsl.wear.data.mappers.GraphQLResponseMapper.updateLegWithRealTimeData(
                leg = currentLeg,
                tripStatus = tripStatus,
                targetStopId = currentLeg.toStopId
            )

            // Update only current leg in route state (use the original route state's currentIndex to preserve navigation state)
            val updatedLegs = routeState.legs.mapIndexed { index, leg ->
                if (index == routeState.currentIndex) updatedLeg else leg
            }

            routeState.copy(
                legs = updatedLegs,
                lastUpdated = System.currentTimeMillis()
            )
        }
    }
}

