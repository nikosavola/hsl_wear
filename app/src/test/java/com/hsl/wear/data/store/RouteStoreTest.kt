package com.hsl.wear.data.store

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.hsl.wear.data.models.FavoriteRoute
import com.hsl.wear.data.models.Location
import com.hsl.wear.data.models.LocationType
import com.hsl.wear.data.models.RouteState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.launch
import kotlinx.coroutines.joinAll
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class RouteStoreTest {

    @get:Rule
    val tmpFolder: TemporaryFolder = TemporaryFolder.builder().assureDeletion().build()

    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var routeStore: RouteStore

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        dataStore = PreferenceDataStoreFactory.create(
            produceFile = { tmpFolder.newFile("test_datastore.preferences_pb") }
        )
        routeStore = RouteStore(dataStore)
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun saveAndGetRouteState_success() = runTest {
        val locFrom = Location("loc1", "From", 60.0, 24.0, type = LocationType.STOP)
        val locTo = Location("loc2", "To", 60.1, 24.1, type = LocationType.STOP)
        val routeState = RouteState(itineraryId = "it1", legs = emptyList(), startTimeIso = "2024-01-01T00:00:00Z", fromLocation = locFrom, toLocation = locTo, lastUpdated = 123456L)
        
        routeStore.saveRouteState(routeState)
        
        val flowValue = routeStore.routeStateFlow.first()
        assertEquals(123456L, flowValue?.lastUpdated)
        assertEquals("loc1", flowValue?.fromLocation?.id)
        assertEquals("loc2", flowValue?.toLocation?.id)
    }

    @Test
    fun clearRouteState_success() = runTest {
        val locFrom = Location("loc1", "From", 60.0, 24.0, type = LocationType.STOP)
        val locTo = Location("loc2", "To", 60.1, 24.1, type = LocationType.STOP)
        val routeState = RouteState(itineraryId = "it1", legs = emptyList(), startTimeIso = "2024-01-01T00:00:00Z", fromLocation = locFrom, toLocation = locTo, lastUpdated = 123456L)
        
        routeStore.saveRouteState(routeState)
        routeStore.clearRouteState()
        
        val flowValue = routeStore.routeStateFlow.first()
        assertNull(flowValue)
    }

    @Test
    fun addAndRemoveFavoriteLocation_success() = runTest {
        val location = Location("loc1", "Favorite", 60.0, 24.0, type = LocationType.STOP)
        
        routeStore.addFavoriteLocation(location)
        var favorites = routeStore.favoriteLocationsFlow.first()
        assertEquals(1, favorites.size)
        assertEquals("loc1", favorites[0].id)
        
        routeStore.removeFavoriteLocation("loc1")
        favorites = routeStore.favoriteLocationsFlow.first()
        assertEquals(0, favorites.size)
    }

    @Test
    fun favoriteLocations_capAt10() = runTest {
        for (i in 1..15) {
            routeStore.addFavoriteLocation(Location("loc$i", "Name$i", 60.0, 24.0, type = LocationType.STOP))
        }
        val favorites = routeStore.favoriteLocationsFlow.first()
        assertEquals(10, favorites.size)
        // Since it adds to end and removes from start, last element should be 15
        assertEquals("loc15", favorites.last().id)
        assertEquals("loc6", favorites.first().id)
    }

    @Test
    fun addFavoriteRoute_duplicateRemoved() = runTest {
        val loc1 = Location("loc1", "Loc1", 60.0, 24.0, type = LocationType.STOP)
        val loc2 = Location("loc2", "Loc2", 60.1, 24.1, type = LocationType.STOP)
        val route1 = FavoriteRoute("route1", "Route route1", loc1, loc2)
        val route2 = FavoriteRoute("route2", "Route route2", loc1, loc2)
        
        routeStore.addFavoriteRoute(route1)
        routeStore.addFavoriteRoute(route2)
        
        val routes = routeStore.favoriteRoutesFlow.first()
        assertEquals(1, routes.size)
        assertEquals("route2", routes[0].id)
    }

    @Test
    fun addFavoriteRoute_capAt20() = runTest {
        for (i in 1..25) {
            val locFrom = Location("from$i", "From$i", 60.0, 24.0, type = LocationType.STOP)
            val locTo = Location("to$i", "To$i", 60.0, 24.0, type = LocationType.STOP)
            routeStore.addFavoriteRoute(FavoriteRoute("route$i", "Route route$i", locFrom, locTo))
        }
        val routes = routeStore.favoriteRoutesFlow.first()
        assertEquals(20, routes.size)
        // Since it adds to beginning (index 0) and removes from end
        assertEquals("route25", routes.first().id)
        assertEquals("route6", routes.last().id)
    }

    @Test
    fun recentLocations_capAt20() = runTest {
        for (i in 1..25) {
            routeStore.addRecentLocation(Location("loc$i", "Name$i", 60.0, 24.0, type = LocationType.STOP))
        }
        val recent = routeStore.recentLocationsFlow.first()
        assertEquals(20, recent.size)
        // Since it adds to beginning (index 0) and removes from end
        assertEquals("loc25", recent.first().id)
        assertEquals("loc6", recent.last().id)
    }

    @Test
    fun addFavoriteRoute_concurrentMutation_isSafe() = runTest {
        // Simulate concurrent addition
        val jobs = (1..100).map { i ->
            launch(Dispatchers.Default) {
                val locFrom = Location("from$i", "From$i", 60.0, 24.0, type = LocationType.STOP)
                val locTo = Location("to$i", "To$i", 60.0, 24.0, type = LocationType.STOP)
                routeStore.addFavoriteRoute(FavoriteRoute("route$i", "Route $i", locFrom, locTo))
            }
        }
        jobs.joinAll()
        
        val routes = routeStore.favoriteRoutesFlow.first()
        // Should contain 20 items (the cap), and no crash should happen
        assertEquals(20, routes.size)
    }

    @Test
    fun favoriteRoutesFlow_withCorruptJson_fallsBackToEmptyAndRecovers() = runTest {
        // Pre-seed the store with a payload that is not valid JSON for the key.
        dataStore.edit { prefs ->
            prefs[stringPreferencesKey("favorite_routes")] = "{ not valid json"
        }

        // Reading must not crash; it degrades to an empty list (issue 002).
        assertEquals(emptyList<FavoriteRoute>(), routeStore.favoriteRoutesFlow.first())

        // A subsequent valid write overwrites the corrupt payload and succeeds.
        val locFrom = Location("from", "From", 60.0, 24.0, type = LocationType.STOP)
        val locTo = Location("to", "To", 60.1, 24.1, type = LocationType.STOP)
        routeStore.addFavoriteRoute(FavoriteRoute("route", "Route", locFrom, locTo))

        val routes = routeStore.favoriteRoutesFlow.first()
        assertEquals(1, routes.size)
        assertEquals("route", routes.first().id)
    }
}
