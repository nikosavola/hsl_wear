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

    @Test
    fun getTopResults_ordersByRelevance() {
        val result1 = AutocompleteResult("id1", "Helsinki", LocationType.STOP, 60.1, 24.9, listOf("line1"))
        val result2 = AutocompleteResult("id2", "Helsinki Central", LocationType.STOP, 60.1, 24.9, listOf("line1", "line2", "line3"))
        val result3 = AutocompleteResult("id3", "Espoo", LocationType.STOP, 60.2, 24.6, emptyList())
        
        val context = LocationContext()
        val results = ranker.getTopResults(listOf(result1, result2, result3), "helsinki", context)
        
        assertEquals(3, results.size)
        // Exact match should be first
        assertEquals("id1", results[0].id)
        // Starts with query, but has more lines, let's just assert top 2 contain Helsinki
        assertEquals(true, results[0].name.contains("Helsinki"))
        assertEquals(true, results[1].name.contains("Helsinki"))
    }

    @Test
    fun getTopResults_appliesProximityBonus() {
        // Same names, different locations
        val result1 = AutocompleteResult("id1", "Stop", LocationType.STOP, 60.1, 24.9, emptyList())
        val result2 = AutocompleteResult("id2", "Stop", LocationType.STOP, 60.5, 24.5, emptyList()) // Far away
        
        val context = LocationContext(currentLocation = Location("current", "Current", 60.1001, 24.9001))
        val results = ranker.getTopResults(listOf(result1, result2), "stop", context)
        
        assertEquals(2, results.size)
        // Closer one should be first
        assertEquals("id1", results[0].id)
    }

    @Test
    fun getTopResults_appliesFavoriteBonus() {
        val result1 = AutocompleteResult("id1", "Stop1", LocationType.STOP, 60.1, 24.9, emptyList())
        val result2 = AutocompleteResult("id2", "Stop2", LocationType.STOP, 60.2, 24.6, emptyList())
        
        val context = LocationContext(favoriteLocations = listOf(Location("id2", "Stop2", 60.2, 24.6)))
        val results = ranker.getTopResults(listOf(result1, result2), "stop", context)
        
        // Favorite should be first
        assertEquals("id2", results[0].id)
    }

    @Test
    fun getTopResults_appliesRecentBonus() {
        val result1 = AutocompleteResult("id1", "Stop1", LocationType.STOP, 60.1, 24.9, emptyList())
        val result2 = AutocompleteResult("id2", "Stop2", LocationType.STOP, 60.2, 24.6, emptyList())
        
        val currentTime = System.currentTimeMillis()
        val recentLocation = Location("id2", "Stop2", 60.2, 24.6, lastUsed = currentTime - 1000) // used 1 second ago
        val context = LocationContext(recentLocations = listOf(recentLocation), currentTime = currentTime)
        
        val results = ranker.getTopResults(listOf(result1, result2), "stop", context)
        
        // Recent should be first
        assertEquals("id2", results[0].id)
    }
}
