package com.hsl.wear.data.store

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.hsl.wear.data.models.RouteState
import com.hsl.wear.data.models.Location
import com.hsl.wear.data.models.FavoriteRoute
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RouteStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = false
    }

    companion object {
        private val ROUTE_STATE_KEY = stringPreferencesKey("route_state")
        private val FAVORITE_LOCATIONS_KEY = stringPreferencesKey("favorite_locations")
        private val FAVORITE_ROUTES_KEY = stringPreferencesKey("favorite_routes")
        private val RECENT_LOCATIONS_KEY = stringPreferencesKey("recent_locations")
        private val LAST_FROM_LOCATION_KEY = stringPreferencesKey("last_from_location")
        private val LAST_TO_LOCATION_KEY = stringPreferencesKey("last_to_location")
    }

    // Route State
    val routeStateFlow: Flow<RouteState?> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[ROUTE_STATE_KEY]?.let { jsonString ->
                try {
                    json.decodeFromString<RouteState>(jsonString)
                } catch (e: Exception) {
                    null
                }
            }
        }

    suspend fun saveRouteState(routeState: RouteState) {
        try {
            dataStore.edit { preferences ->
                preferences[ROUTE_STATE_KEY] = json.encodeToString(routeState)
            }
        } catch (e: Exception) {
            android.util.Log.e("RouteStore", "DataStore error", e)
        }
    }

    suspend fun clearRouteState() {
        try {
            dataStore.edit { preferences ->
                preferences.remove(ROUTE_STATE_KEY)
            }
        } catch (e: Exception) {
            android.util.Log.e("RouteStore", "DataStore error", e)
        }
    }

    // Favorite Locations
    val favoriteLocationsFlow: Flow<List<Location>> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[FAVORITE_LOCATIONS_KEY]?.let { jsonString ->
                try {
                    json.decodeFromString<List<Location>>(jsonString)
                } catch (e: Exception) {
                    emptyList()
                }
            } ?: emptyList()
        }

    suspend fun addFavoriteLocation(location: Location) {
        try {
            val currentFavorites = favoriteLocationsFlow.first().toMutableList()
            // Remove if already exists, then add to end
            currentFavorites.removeAll { it.id == location.id }
            currentFavorites.add(location)
            // Keep only last 10 favorites
            if (currentFavorites.size > 10) {
                currentFavorites.removeAt(0)
            }

            dataStore.edit { preferences ->
                preferences[FAVORITE_LOCATIONS_KEY] = json.encodeToString(currentFavorites)
            }
        } catch (e: Exception) {
            android.util.Log.e("RouteStore", "DataStore error", e)
        }
    }

    suspend fun removeFavoriteLocation(locationId: String) {
        try {
            val currentFavorites = favoriteLocationsFlow.first().toMutableList()
            currentFavorites.removeAll { it.id == locationId }

            dataStore.edit { preferences ->
                preferences[FAVORITE_LOCATIONS_KEY] = json.encodeToString(currentFavorites)
            }
        } catch (e: Exception) {
            android.util.Log.e("RouteStore", "DataStore error", e)
        }
    }

    // Favorite Routes
    val favoriteRoutesFlow: Flow<List<FavoriteRoute>> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[FAVORITE_ROUTES_KEY]?.let { jsonString ->
                try {
                    json.decodeFromString<List<FavoriteRoute>>(jsonString)
                } catch (e: Exception) {
                    emptyList()
                }
            } ?: emptyList()
        }

    suspend fun addFavoriteRoute(favoriteRoute: FavoriteRoute) {
        try {
            val currentFavorites = favoriteRoutesFlow.first().toMutableList()
            // Remove if already exists (same from/to combination)
            currentFavorites.removeAll {
                it.fromLocation.id == favoriteRoute.fromLocation.id &&
                it.toLocation.id == favoriteRoute.toLocation.id
            }
            currentFavorites.add(0, favoriteRoute) // Add to beginning
            // Keep only last 20 favorites
            if (currentFavorites.size > 20) {
                currentFavorites.removeAt(currentFavorites.size - 1)
            }

            dataStore.edit { preferences ->
                preferences[FAVORITE_ROUTES_KEY] = json.encodeToString(currentFavorites)
            }
        } catch (e: Exception) {
            android.util.Log.e("RouteStore", "DataStore error", e)
        }
    }

    suspend fun removeFavoriteRoute(routeId: String) {
        try {
            val currentFavorites = favoriteRoutesFlow.first().toMutableList()
            currentFavorites.removeAll { it.id == routeId }

            dataStore.edit { preferences ->
                preferences[FAVORITE_ROUTES_KEY] = json.encodeToString(currentFavorites)
            }
        } catch (e: Exception) {
            android.util.Log.e("RouteStore", "DataStore error", e)
        }
    }

    // Recent Locations
    val recentLocationsFlow: Flow<List<Location>> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[RECENT_LOCATIONS_KEY]?.let { jsonString ->
                try {
                    json.decodeFromString<List<Location>>(jsonString)
                } catch (e: Exception) {
                    emptyList()
                }
            } ?: emptyList()
        }

    suspend fun addRecentLocation(location: Location) {
        try {
            val currentRecent = recentLocationsFlow.first().toMutableList()
            // Remove if already exists, then add to end
            currentRecent.removeAll { it.id == location.id }
            currentRecent.add(0, location) // Add to beginning
            // Keep only last 20 recent locations
            if (currentRecent.size > 20) {
                currentRecent.removeAt(currentRecent.size - 1)
            }

            dataStore.edit { preferences ->
                preferences[RECENT_LOCATIONS_KEY] = json.encodeToString(currentRecent)
            }
        } catch (e: Exception) {
            android.util.Log.e("RouteStore", "DataStore error", e)
        }
    }

    // Last used locations
    val lastFromLocationFlow: Flow<Location?> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[LAST_FROM_LOCATION_KEY]?.let { jsonString ->
                try {
                    json.decodeFromString<Location>(jsonString)
                } catch (e: Exception) {
                    null
                }
            }
        }

    val lastToLocationFlow: Flow<Location?> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[LAST_TO_LOCATION_KEY]?.let { jsonString ->
                try {
                    json.decodeFromString<Location>(jsonString)
                } catch (e: Exception) {
                    null
                }
            }
        }

    suspend fun saveLastFromLocation(location: Location) {
        try {
            dataStore.edit { preferences ->
                preferences[LAST_FROM_LOCATION_KEY] = json.encodeToString(location)
            }
        } catch (e: Exception) {
            android.util.Log.e("RouteStore", "DataStore error", e)
        }
    }

    suspend fun saveLastToLocation(location: Location) {
        try {
            dataStore.edit { preferences ->
                preferences[LAST_TO_LOCATION_KEY] = json.encodeToString(location)
            }
        } catch (e: Exception) {
            android.util.Log.e("RouteStore", "DataStore error", e)
        }
    }

    // Utility method to clear all stored data
    suspend fun clearAllData() {
        try {
            dataStore.edit { preferences ->
                preferences.clear()
            }
        } catch (e: Exception) {
            android.util.Log.e("RouteStore", "DataStore error", e)
        }
    }
}