import re

with open("app/src/main/java/com/hsl/wear/data/store/RouteStore.kt", "r") as f:
    content = f.read()

# Replace android.util.Log import
if "import android.util.Log" not in content:
    content = content.replace("import java.io.IOException", "import android.util.Log\nimport java.io.IOException")

# Fix routeStateFlow
content = content.replace('''        .map { preferences ->
            preferences[ROUTE_STATE_KEY]?.let { jsonString ->
                try {
                    json.decodeFromString<RouteState>(jsonString)
                } catch (e: Exception) {
                    null
                }
            }
        }''', '''        .map { preferences ->
            preferences[ROUTE_STATE_KEY]?.let { jsonString ->
                try {
                    json.decodeFromString<RouteState>(jsonString)
                } catch (e: Exception) {
                    Log.e("RouteStore", "Failed to decode RouteState payload", e)
                    null
                }
            }
        }''')

# Fix favoriteLocationsFlow
content = content.replace('''        .map { preferences ->
            preferences[FAVORITE_LOCATIONS_KEY]?.let { jsonString ->
                try {
                    json.decodeFromString<List<Location>>(jsonString)
                } catch (e: Exception) {
                    emptyList()
                }
            } ?: emptyList()
        }''', '''        .map { preferences ->
            preferences[FAVORITE_LOCATIONS_KEY]?.let { jsonString ->
                try {
                    json.decodeFromString<List<Location>>(jsonString)
                } catch (e: Exception) {
                    Log.e("RouteStore", "Failed to decode List<Location> payload", e)
                    emptyList()
                }
            } ?: emptyList()
        }''')

# Fix favoriteRoutesFlow
content = content.replace('''        .map { preferences ->
            preferences[FAVORITE_ROUTES_KEY]?.let { jsonString ->
                try {
                    json.decodeFromString<List<FavoriteRoute>>(jsonString)
                } catch (e: Exception) {
                    emptyList()
                }
            } ?: emptyList()
        }''', '''        .map { preferences ->
            preferences[FAVORITE_ROUTES_KEY]?.let { jsonString ->
                try {
                    json.decodeFromString<List<FavoriteRoute>>(jsonString)
                } catch (e: Exception) {
                    Log.e("RouteStore", "Failed to decode List<FavoriteRoute> payload", e)
                    emptyList()
                }
            } ?: emptyList()
        }''')

# Fix recentLocationsFlow
content = content.replace('''        .map { preferences ->
            preferences[RECENT_LOCATIONS_KEY]?.let { jsonString ->
                try {
                    json.decodeFromString<List<Location>>(jsonString)
                } catch (e: Exception) {
                    emptyList()
                }
            } ?: emptyList()
        }''', '''        .map { preferences ->
            preferences[RECENT_LOCATIONS_KEY]?.let { jsonString ->
                try {
                    json.decodeFromString<List<Location>>(jsonString)
                } catch (e: Exception) {
                    Log.e("RouteStore", "Failed to decode List<Location> payload", e)
                    emptyList()
                }
            } ?: emptyList()
        }''')

# Fix lastFromLocationFlow
content = content.replace('''        .map { preferences ->
            preferences[LAST_FROM_LOCATION_KEY]?.let { jsonString ->
                try {
                    json.decodeFromString<Location>(jsonString)
                } catch (e: Exception) {
                    null
                }
            }
        }''', '''        .map { preferences ->
            preferences[LAST_FROM_LOCATION_KEY]?.let { jsonString ->
                try {
                    json.decodeFromString<Location>(jsonString)
                } catch (e: Exception) {
                    Log.e("RouteStore", "Failed to decode Location payload", e)
                    null
                }
            }
        }''')

# Fix lastToLocationFlow
content = content.replace('''        .map { preferences ->
            preferences[LAST_TO_LOCATION_KEY]?.let { jsonString ->
                try {
                    json.decodeFromString<Location>(jsonString)
                } catch (e: Exception) {
                    null
                }
            }
        }''', '''        .map { preferences ->
            preferences[LAST_TO_LOCATION_KEY]?.let { jsonString ->
                try {
                    json.decodeFromString<Location>(jsonString)
                } catch (e: Exception) {
                    Log.e("RouteStore", "Failed to decode Location payload", e)
                    null
                }
            }
        }''')

with open("app/src/main/java/com/hsl/wear/data/store/RouteStore.kt", "w") as f:
    f.write(content)
