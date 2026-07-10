package com.hsl.wear.data.store

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.hsl.wear.data.models.FavoriteRoute
import com.hsl.wear.data.models.Location
import com.hsl.wear.data.models.LocationType
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit

class RouteStoreTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private fun createDataStore(): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            produceFile = { File(tempFolder.root, "test_preferences.preferences_pb") }
        )
    }

    @Test
    fun testConcurrentAddFavoriteRoute() = runBlocking {
        val dataStore = createDataStore()
        val routeStore = RouteStore(dataStore)

        // Run multiple concurrent adds
        val count = 50
        coroutineScope {
            val deferreds = (1..count).map { i ->
                async {
                    val locationFrom = Location(id = "from_$i", name = "From $i", lat = 0.0, lon = 0.0, type = LocationType.STOP)
                    val locationTo = Location(id = "to_$i", name = "To $i", lat = 0.0, lon = 0.0, type = LocationType.STOP)
                    val route = FavoriteRoute(id = "route_$i", name = "Route $i", fromLocation = locationFrom, toLocation = locationTo)
                    routeStore.addFavoriteRoute(route)
                }
            }
            deferreds.awaitAll()
        }

        val favorites = routeStore.favoriteRoutesFlow.first()
        
        // Since we keep the last 20 favorites, and 50 were added concurrently,
        // there should be exactly 20 elements.
        assertEquals(20, favorites.size)
    }

    @Test
    fun testCorruptJsonFallback() = runBlocking {
        val dataStore = createDataStore()
        
        // Write corrupt JSON explicitly
        dataStore.edit { prefs ->
            val key = stringPreferencesKey("favorite_routes")
            prefs[key] = "{ invalid json"
        }

        val routeStore = RouteStore(dataStore)
        val favorites = routeStore.favoriteRoutesFlow.first()

        // It should fallback to empty list instead of crashing
        assertTrue(favorites.isEmpty())
        
        // Adding a valid route should clear the corrupt key and save properly
        val locationFrom = Location(id = "from", name = "From", lat = 0.0, lon = 0.0, type = LocationType.STOP)
        val locationTo = Location(id = "to", name = "To", lat = 0.0, lon = 0.0, type = LocationType.STOP)
        val route = FavoriteRoute(id = "route", name = "Route", fromLocation = locationFrom, toLocation = locationTo)
        
        val result = routeStore.addFavoriteRoute(route)
        assertTrue(result.isSuccess)
        
        val newFavorites = routeStore.favoriteRoutesFlow.first()
        assertEquals(1, newFavorites.size)
        assertEquals("route", newFavorites[0].id)
    }
}
