import re

with open("app/src/main/java/com/hsl/wear/data/store/RouteStore.kt", "r") as f:
    content = f.read()

# Replace saveRouteState
content = re.sub(
    r'suspend fun saveRouteState\(routeState: RouteState\)\s*\{\s*try\s*\{\s*dataStore\.edit\s*\{\s*preferences\s*->\s*preferences\[ROUTE_STATE_KEY\]\s*=\s*json\.encodeToString\(routeState\)\s*\}\s*\}\s*catch\s*\(e:\s*Exception\)\s*\{\s*// Log error in production\s*\}\s*\}',
    '''suspend fun saveRouteState(routeState: RouteState): Result<Unit> {
        return try {
            dataStore.edit { preferences ->
                preferences[ROUTE_STATE_KEY] = json.encodeToString(routeState)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("RouteStore", "Failed to save route state", e)
            Result.failure(e)
        }
    }''', content, flags=re.MULTILINE | re.DOTALL)

# Replace clearRouteState
content = re.sub(
    r'suspend fun clearRouteState\(\)\s*\{\s*try\s*\{\s*dataStore\.edit\s*\{\s*preferences\s*->\s*preferences\.remove\(ROUTE_STATE_KEY\)\s*\}\s*\}\s*catch\s*\(e:\s*Exception\)\s*\{\s*// Log error in production\s*\}\s*\}',
    '''suspend fun clearRouteState(): Result<Unit> {
        return try {
            dataStore.edit { preferences ->
                preferences.remove(ROUTE_STATE_KEY)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("RouteStore", "Failed to clear route state", e)
            Result.failure(e)
        }
    }''', content, flags=re.MULTILINE | re.DOTALL)

# Replace addFavoriteLocation
content = re.sub(
    r'suspend fun addFavoriteLocation\(location: Location\)\s*\{\s*try\s*\{\s*val currentFavorites = favoriteLocationsFlow\.first\(\)\.toMutableList\(\).*?preferences\[FAVORITE_LOCATIONS_KEY\]\s*=\s*json\.encodeToString\(currentFavorites\)\s*\}\s*\}\s*catch\s*\(e:\s*Exception\)\s*\{\s*// Log error in production\s*\}\s*\}',
    '''suspend fun addFavoriteLocation(location: Location): Result<Unit> {
        return try {
            dataStore.edit { preferences ->
                val currentJson = preferences[FAVORITE_LOCATIONS_KEY]
                val currentFavorites = if (currentJson != null) {
                    try {
                        json.decodeFromString<List<Location>>(currentJson)
                    } catch (e: Exception) {
                        Log.e("RouteStore", "Corrupt JSON for favorite locations", e)
                        emptyList()
                    }
                } else emptyList()

                val updatedList = currentFavorites.toMutableList()
                // Remove if already exists, then add to end
                updatedList.removeAll { it.id == location.id }
                updatedList.add(location)
                // Keep only last 10 favorites
                if (updatedList.size > 10) {
                    updatedList.removeAt(0)
                }

                preferences[FAVORITE_LOCATIONS_KEY] = json.encodeToString(updatedList)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("RouteStore", "Failed to add favorite location", e)
            Result.failure(e)
        }
    }''', content, flags=re.MULTILINE | re.DOTALL)

# Replace removeFavoriteLocation
content = re.sub(
    r'suspend fun removeFavoriteLocation\(locationId: String\)\s*\{\s*try\s*\{\s*val currentFavorites = favoriteLocationsFlow\.first\(\)\.toMutableList\(\).*?preferences\[FAVORITE_LOCATIONS_KEY\]\s*=\s*json\.encodeToString\(currentFavorites\)\s*\}\s*\}\s*catch\s*\(e:\s*Exception\)\s*\{\s*// Log error in production\s*\}\s*\}',
    '''suspend fun removeFavoriteLocation(locationId: String): Result<Unit> {
        return try {
            dataStore.edit { preferences ->
                val currentJson = preferences[FAVORITE_LOCATIONS_KEY]
                val currentFavorites = if (currentJson != null) {
                    try {
                        json.decodeFromString<List<Location>>(currentJson)
                    } catch (e: Exception) {
                        Log.e("RouteStore", "Corrupt JSON for favorite locations", e)
                        emptyList()
                    }
                } else emptyList()

                val updatedList = currentFavorites.toMutableList()
                updatedList.removeAll { it.id == locationId }

                preferences[FAVORITE_LOCATIONS_KEY] = json.encodeToString(updatedList)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("RouteStore", "Failed to remove favorite location", e)
            Result.failure(e)
        }
    }''', content, flags=re.MULTILINE | re.DOTALL)

# Replace addFavoriteRoute
content = re.sub(
    r'suspend fun addFavoriteRoute\(favoriteRoute: FavoriteRoute\)\s*\{\s*try\s*\{\s*val currentFavorites = favoriteRoutesFlow\.first\(\)\.toMutableList\(\).*?preferences\[FAVORITE_ROUTES_KEY\]\s*=\s*json\.encodeToString\(currentFavorites\)\s*\}\s*\}\s*catch\s*\(e:\s*Exception\)\s*\{\s*// Log error in production\s*\}\s*\}',
    '''suspend fun addFavoriteRoute(favoriteRoute: FavoriteRoute): Result<Unit> {
        return try {
            dataStore.edit { preferences ->
                val currentJson = preferences[FAVORITE_ROUTES_KEY]
                val currentFavorites = if (currentJson != null) {
                    try {
                        json.decodeFromString<List<FavoriteRoute>>(currentJson)
                    } catch (e: Exception) {
                        Log.e("RouteStore", "Corrupt JSON for favorite routes", e)
                        emptyList()
                    }
                } else emptyList()

                val updatedList = currentFavorites.toMutableList()
                // Remove if already exists (same from/to combination)
                updatedList.removeAll {
                    it.fromLocation.id == favoriteRoute.fromLocation.id &&
                    it.toLocation.id == favoriteRoute.toLocation.id
                }
                updatedList.add(0, favoriteRoute) // Add to beginning
                // Keep only last 20 favorites
                if (updatedList.size > 20) {
                    updatedList.removeAt(updatedList.size - 1)
                }

                preferences[FAVORITE_ROUTES_KEY] = json.encodeToString(updatedList)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("RouteStore", "Failed to add favorite route", e)
            Result.failure(e)
        }
    }''', content, flags=re.MULTILINE | re.DOTALL)

# Replace removeFavoriteRoute
content = re.sub(
    r'suspend fun removeFavoriteRoute\(routeId: String\)\s*\{\s*try\s*\{\s*val currentFavorites = favoriteRoutesFlow\.first\(\)\.toMutableList\(\).*?preferences\[FAVORITE_ROUTES_KEY\]\s*=\s*json\.encodeToString\(currentFavorites\)\s*\}\s*\}\s*catch\s*\(e:\s*Exception\)\s*\{\s*// Log error in production\s*\}\s*\}',
    '''suspend fun removeFavoriteRoute(routeId: String): Result<Unit> {
        return try {
            dataStore.edit { preferences ->
                val currentJson = preferences[FAVORITE_ROUTES_KEY]
                val currentFavorites = if (currentJson != null) {
                    try {
                        json.decodeFromString<List<FavoriteRoute>>(currentJson)
                    } catch (e: Exception) {
                        Log.e("RouteStore", "Corrupt JSON for favorite routes", e)
                        emptyList()
                    }
                } else emptyList()

                val updatedList = currentFavorites.toMutableList()
                updatedList.removeAll { it.id == routeId }

                preferences[FAVORITE_ROUTES_KEY] = json.encodeToString(updatedList)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("RouteStore", "Failed to remove favorite route", e)
            Result.failure(e)
        }
    }''', content, flags=re.MULTILINE | re.DOTALL)

# Replace addRecentLocation
content = re.sub(
    r'suspend fun addRecentLocation\(location: Location\)\s*\{\s*try\s*\{\s*val currentRecent = recentLocationsFlow\.first\(\)\.toMutableList\(\).*?preferences\[RECENT_LOCATIONS_KEY\]\s*=\s*json\.encodeToString\(currentRecent\)\s*\}\s*\}\s*catch\s*\(e:\s*Exception\)\s*\{\s*// Log error in production\s*\}\s*\}',
    '''suspend fun addRecentLocation(location: Location): Result<Unit> {
        return try {
            dataStore.edit { preferences ->
                val currentJson = preferences[RECENT_LOCATIONS_KEY]
                val currentRecent = if (currentJson != null) {
                    try {
                        json.decodeFromString<List<Location>>(currentJson)
                    } catch (e: Exception) {
                        Log.e("RouteStore", "Corrupt JSON for recent locations", e)
                        emptyList()
                    }
                } else emptyList()

                val updatedList = currentRecent.toMutableList()
                // Remove if already exists, then add to end
                updatedList.removeAll { it.id == location.id }
                updatedList.add(0, location) // Add to beginning
                // Keep only last 20 recent locations
                if (updatedList.size > 20) {
                    updatedList.removeAt(updatedList.size - 1)
                }

                preferences[RECENT_LOCATIONS_KEY] = json.encodeToString(updatedList)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("RouteStore", "Failed to add recent location", e)
            Result.failure(e)
        }
    }''', content, flags=re.MULTILINE | re.DOTALL)

# Replace saveLastFromLocation
content = re.sub(
    r'suspend fun saveLastFromLocation\(location: Location\)\s*\{\s*try\s*\{\s*dataStore\.edit\s*\{\s*preferences\s*->\s*preferences\[LAST_FROM_LOCATION_KEY\]\s*=\s*json\.encodeToString\(location\)\s*\}\s*\}\s*catch\s*\(e:\s*Exception\)\s*\{\s*// Log error in production\s*\}\s*\}',
    '''suspend fun saveLastFromLocation(location: Location): Result<Unit> {
        return try {
            dataStore.edit { preferences ->
                preferences[LAST_FROM_LOCATION_KEY] = json.encodeToString(location)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("RouteStore", "Failed to save last from location", e)
            Result.failure(e)
        }
    }''', content, flags=re.MULTILINE | re.DOTALL)

# Replace saveLastToLocation
content = re.sub(
    r'suspend fun saveLastToLocation\(location: Location\)\s*\{\s*try\s*\{\s*dataStore\.edit\s*\{\s*preferences\s*->\s*preferences\[LAST_TO_LOCATION_KEY\]\s*=\s*json\.encodeToString\(location\)\s*\}\s*\}\s*catch\s*\(e:\s*Exception\)\s*\{\s*// Log error in production\s*\}\s*\}',
    '''suspend fun saveLastToLocation(location: Location): Result<Unit> {
        return try {
            dataStore.edit { preferences ->
                preferences[LAST_TO_LOCATION_KEY] = json.encodeToString(location)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("RouteStore", "Failed to save last to location", e)
            Result.failure(e)
        }
    }''', content, flags=re.MULTILINE | re.DOTALL)

# Replace clearAllData
content = re.sub(
    r'suspend fun clearAllData\(\)\s*\{\s*try\s*\{\s*dataStore\.edit\s*\{\s*preferences\s*->\s*preferences\.clear\(\)\s*\}\s*\}\s*catch\s*\(e:\s*Exception\)\s*\{\s*// Log error in production\s*\}\s*\}',
    '''suspend fun clearAllData(): Result<Unit> {
        return try {
            dataStore.edit { preferences ->
                preferences.clear()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("RouteStore", "Failed to clear all data", e)
            Result.failure(e)
        }
    }''', content, flags=re.MULTILINE | re.DOTALL)

with open("app/src/main/java/com/hsl/wear/data/store/RouteStore.kt", "w") as f:
    f.write(content)

