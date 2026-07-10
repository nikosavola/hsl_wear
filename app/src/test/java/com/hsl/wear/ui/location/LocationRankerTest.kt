package com.hsl.wear.ui.location

import com.hsl.wear.data.models.AutocompleteResult
import com.hsl.wear.data.models.Location
import com.hsl.wear.data.models.LocationType
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class LocationRankerTest {

    private lateinit var ranker: LocationRanker

    @Before
    fun setup() {
        ranker = LocationRanker()
    }

    private fun result(id: String, name: String, lat: Double, lon: Double, lines: List<String> = emptyList()) =
        AutocompleteResult(id = id, name = name, lat = lat, lon = lon, type = LocationType.STOP, lines = lines)

    @Test
    fun getTopResults_ordersByRelevance() {
        val result1 = result("id1", "Helsinki", 60.1, 24.9, listOf("line1"))
        val result2 = result("id2", "Helsinki Central", 60.1, 24.9, listOf("line1", "line2", "line3"))
        val result3 = result("id3", "Espoo", 60.2, 24.6)

        val context = LocationContext()
        val results = ranker.getTopResults(listOf(result1, result2, result3), "helsinki", context)

        assertEquals(3, results.size)
        // Exact match should be first
        assertEquals("id1", results[0].id)
        // Both top results should be the Helsinki entries
        assertEquals(true, results[0].name.contains("Helsinki"))
        assertEquals(true, results[1].name.contains("Helsinki"))
    }

    @Test
    fun getTopResults_appliesProximityBonus() {
        // Same names, different locations
        val result1 = result("id1", "Stop", 60.1, 24.9)
        val result2 = result("id2", "Stop", 60.5, 24.5) // Far away

        val context = LocationContext(
            currentLocation = Location("current", "Current", 60.1001, 24.9001, type = LocationType.STOP)
        )
        val results = ranker.getTopResults(listOf(result1, result2), "stop", context)

        assertEquals(2, results.size)
        // Closer one should be first
        assertEquals("id1", results[0].id)
    }

    @Test
    fun getTopResults_appliesFavoriteBonus() {
        val result1 = result("id1", "Stop1", 60.1, 24.9)
        val result2 = result("id2", "Stop2", 60.2, 24.6)

        val context = LocationContext(
            favoriteLocations = listOf(Location("id2", "Stop2", 60.2, 24.6, type = LocationType.STOP))
        )
        val results = ranker.getTopResults(listOf(result1, result2), "stop", context)

        // Favorite should be first
        assertEquals("id2", results[0].id)
    }

    @Test
    fun getTopResults_appliesRecentBonus() {
        val result1 = result("id1", "Stop1", 60.1, 24.9)
        val result2 = result("id2", "Stop2", 60.2, 24.6)

        val currentTime = System.currentTimeMillis()
        val recentLocation = Location(
            "id2", "Stop2", 60.2, 24.6, type = LocationType.STOP, lastUsed = currentTime - 1000
        ) // used 1 second ago
        val context = LocationContext(recentLocations = listOf(recentLocation), currentTime = currentTime)

        val results = ranker.getTopResults(listOf(result1, result2), "stop", context)

        // Recent should be first
        assertEquals("id2", results[0].id)
    }
}
