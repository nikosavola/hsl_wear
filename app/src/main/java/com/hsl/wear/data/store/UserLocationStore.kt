package com.hsl.wear.data.store

import com.hsl.wear.data.models.Location
import com.hsl.wear.data.models.LocationType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserLocationStore @Inject constructor() {

    private val _favoriteLocations = MutableStateFlow<List<Location>>(emptyList())
    val favoriteLocations: Flow<List<Location>> = _favoriteLocations.asStateFlow()

    private val _recentLocations = MutableStateFlow<List<Location>>(emptyList())
    val recentLocations: Flow<List<Location>> = _recentLocations.asStateFlow()

    // Maximum number of recent locations to keep
    private val maxRecentLocations = 10

    init {
        // Initialize with some sample data (in real app, this would load from storage)
        _favoriteLocations.value = listOf(
            Location(
                id = "home_sample",
                name = "Home",
                lat = 60.1699,
                lon = 24.9384,
                type = LocationType.ADDRESS
            ),
            Location(
                id = "work_sample",
                name = "Work",
                lat = 60.1850,
                lon = 24.8300,
                type = LocationType.ADDRESS
            )
        )
    }

    fun addToFavorites(location: Location) {
        val current = _favoriteLocations.value.toMutableList()
        // Remove if already exists, then add to front
        current.removeAll { it.id == location.id }
        current.add(0, location)
        _favoriteLocations.value = current
    }

    fun removeFromFavorites(locationId: String) {
        val current = _favoriteLocations.value.toMutableList()
        current.removeAll { it.id == locationId }
        _favoriteLocations.value = current
    }

    fun isFavorite(locationId: String): Boolean {
        return _favoriteLocations.value.any { it.id == locationId }
    }

    fun addToRecent(location: Location) {
        val current = _recentLocations.value.toMutableList()
        val currentTime = System.currentTimeMillis()

        // Remove if already exists
        current.removeAll { it.id == location.id }

        // Create new location with updated timestamp
        val updatedLocation = location.copy(lastUsed = currentTime)

        // Add to front
        current.add(0, updatedLocation)

        // Keep only max recent locations
        if (current.size > maxRecentLocations) {
            _recentLocations.value = current.take(maxRecentLocations)
        } else {
            _recentLocations.value = current
        }
    }

    fun getRecentLocations(): List<Location> {
        return _recentLocations.value
    }

    fun getFavoriteLocations(): List<Location> {
        return _favoriteLocations.value
    }

    fun toggleFavorite(location: Location) {
        if (isFavorite(location.id)) {
            removeFromFavorites(location.id)
        } else {
            addToFavorites(location)
        }
    }
}