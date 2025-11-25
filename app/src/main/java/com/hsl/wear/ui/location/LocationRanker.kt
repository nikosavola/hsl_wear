package com.hsl.wear.ui.location

import com.hsl.wear.data.models.AutocompleteResult
import com.hsl.wear.data.models.Location
import com.hsl.wear.data.models.LocationType
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject
import kotlin.math.*

data class RankedLocation(
    val result: AutocompleteResult,
    val rank: Double,
    val rankReasons: List<String>
)

data class LocationContext(
    val currentLocation: Location? = null,
    val recentLocations: List<Location> = emptyList(),
    val favoriteLocations: List<Location> = emptyList(),
    val currentTime: Long = System.currentTimeMillis()
)

@ViewModelScoped
class LocationRanker @Inject constructor() {

    fun rankSearchResults(
        results: List<AutocompleteResult>,
        query: String,
        context: LocationContext
    ): List<RankedLocation> {
        return results.map { result ->
            val ranked = calculateRank(result, query, context)
            RankedLocation(result, ranked.rank, ranked.rankReasons)
        }.sortedByDescending { it.rank }
    }

    private fun calculateRank(
        result: AutocompleteResult,
        query: String,
        context: LocationContext
    ): RankedLocation {
        var rank = 0.0
        val reasons = mutableListOf<String>()

        // 1. Query relevance (0-40 points)
        val queryScore = calculateQueryRelevance(result, query)
        rank += queryScore * 40
        if (queryScore > 0.8) reasons.add("Exact match")

        // 2. Location type bonus (0-20 points)
        val typeBonus = when (result.type.name.lowercase()) {
            "stop" -> 20
            "address" -> 15
            else -> 5
        }
        rank += typeBonus
        if (typeBonus >= 15) reasons.add("Transit stop")

        // 3. Favorite location bonus (0-25 points)
        val favoriteBonus = calculateFavoriteBonus(result, context.favoriteLocations)
        rank += favoriteBonus
        if (favoriteBonus > 0) reasons.add("Favorite")

        // 4. Recent location bonus (0-15 points)
        val recentBonus = calculateRecentBonus(result, context.recentLocations, context.currentTime)
        rank += recentBonus
        if (recentBonus > 0) reasons.add("Recent")

        // 5. Proximity bonus (0-20 points)
        val proximityBonus = calculateProximityBonus(result, context.currentLocation)
        rank += proximityBonus
        if (proximityBonus > 0) reasons.add("Nearby")

        // 6. Transport lines bonus (0-10 points)
        val linesBonus = calculateLinesBonus(result)
        rank += linesBonus
        if (linesBonus > 0) reasons.add("Major stop")

        return RankedLocation(result, rank, reasons)
    }

    private fun calculateQueryRelevance(result: AutocompleteResult, query: String): Double {
        if (query.isBlank()) return 0.0

        val resultName = result.name.lowercase()
        val queryLower = query.lowercase()

        // Exact match gets highest score
        if (resultName == queryLower) return 1.0

        // Starts with query gets high score
        if (resultName.startsWith(queryLower)) return 0.9

        // Contains query gets medium score
        if (resultName.contains(queryLower)) return 0.7

        // Check words in name
        val queryWords = queryLower.split(" ").filter { it.isNotBlank() }
        val resultWords = resultName.split(" ").filter { it.isNotBlank() }

        val matchingWords = queryWords.count { word ->
            resultWords.any { it.startsWith(word) || word.startsWith(it) }
        }

        return if (queryWords.isNotEmpty()) {
            matchingWords.toDouble() / queryWords.size * 0.5
        } else 0.0
    }

    private fun calculateFavoriteBonus(result: AutocompleteResult, favorites: List<Location>): Double {
        return favorites.find { fav ->
            fav.name.equals(result.name, ignoreCase = true) ||
            fav.id == result.id ||
            (abs(fav.lat - result.lat) < 0.001 && abs(fav.lon - result.lon) < 0.001)
        }?.let { 25.0 } ?: 0.0
    }

    private fun calculateRecentBonus(result: AutocompleteResult, recent: List<Location>, currentTime: Long): Double {
        val matchingRecent = recent.find { recent ->
            recent.name.equals(result.name, ignoreCase = true) ||
            recent.id == result.id ||
            (abs(recent.lat - result.lat) < 0.001 && abs(recent.lon - result.lon) < 0.001)
        }

        return matchingRecent?.let { loc ->
            val hoursSince = (currentTime - loc.lastUsed) / (1000 * 60 * 60)
            when {
                hoursSince < 1 -> 15.0    // Within last hour
                hoursSince < 6 -> 12.0    // Within 6 hours
                hoursSince < 24 -> 8.0    // Within day
                hoursSince < 168 -> 4.0   // Within week
                else -> 0.0
            }
        } ?: 0.0
    }

    private fun calculateProximityBonus(result: AutocompleteResult, currentLocation: Location?): Double {
        if (currentLocation == null) return 0.0

        val distance = calculateDistance(
            currentLocation.lat, currentLocation.lon,
            result.lat, result.lon
        )

        return when {
            distance < 0.5 -> 20.0    // Within 500m
            distance < 1.0 -> 15.0    // Within 1km
            distance < 2.0 -> 10.0    // Within 2km
            distance < 5.0 -> 5.0     // Within 5km
            else -> 0.0
        }
    }

    private fun calculateLinesBonus(result: AutocompleteResult): Double {
        val linesCount = result.lines.size
        return when {
            linesCount >= 10 -> 10.0
            linesCount >= 5 -> 7.0
            linesCount >= 3 -> 5.0
            linesCount >= 1 -> 3.0
            else -> 0.0
        }
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371 // Earth's radius in kilometers

        val latDistance = Math.toRadians(lat2 - lat1)
        val lonDistance = Math.toRadians(lon2 - lon1)

        val a = sin(latDistance / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(lonDistance / 2).pow(2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return r * c
    }

    fun getTopResults(
        results: List<AutocompleteResult>,
        query: String,
        context: LocationContext,
        maxResults: Int = 5
    ): List<AutocompleteResult> {
        val ranked = rankSearchResults(results, query, context)
        return ranked.take(maxResults).map { it.result }
    }
}