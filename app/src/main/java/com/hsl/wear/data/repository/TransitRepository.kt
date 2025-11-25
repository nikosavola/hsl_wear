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

    suspend fun getRealtimeDepartures(stopId: String): Result<List<Leg>> {
        return hslRepository.getRealtimeDepartures(stopId)
    }

    // Route State Management
    val activeRouteState: Flow<RouteState?> = routeStore.routeStateFlow
    val notificationPreferencesFlow: Flow<NotificationPreferences> = routeStore.notificationPreferencesFlow

    suspend fun saveRouteState(routeState: RouteState) {
        routeStore.saveRouteState(routeState)
    }

    suspend fun clearRouteState() {
        routeStore.clearRouteState()
    }

    // Notification Preferences
    suspend fun saveNotificationPreferences(preferences: NotificationPreferences) {
        routeStore.saveNotificationPreferences(preferences)
    }

    suspend fun getNotificationPreferences(): NotificationPreferences {
        return notificationPreferencesFlow.first()
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
}

