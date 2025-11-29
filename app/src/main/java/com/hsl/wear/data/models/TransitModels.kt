package com.hsl.wear.data.models

import com.hsl.wear.utils.TimeFormatter
import kotlinx.serialization.Serializable

@Serializable
data class Leg(
    val mode: String,                  // BUS, TRAM, RAIL, SUBWAY, FERRY, WALK
    val line: String?,                 // "59", "M1", etc. null for WALK (from trip.routeShortName)
    val headsign: String?,             // Trip headsign showing destination (from trip.tripHeadsign)
    val fromStopId: String?,          // Stop ID for origin
    val fromStopName: String,         // Human-readable stop name
    val fromPlatformCode: String?,    // Platform code at departure stop
    val fromZoneId: String?,          // Fare zone for departure stop (A, B, C, D, etc.)
    val toStopId: String?,            // Stop ID for destination
    val toStopName: String,           // Human-readable destination name
    val toPlatformCode: String?,      // Platform code at arrival stop
    val toZoneId: String?,            // Fare zone for arrival stop (A, B, C, D, etc.)
    val platform: String?,            // Legacy: Platform/track number (deprecated, use fromPlatformCode)
    val scheduledTimeIso: String,     // ISO 8601 scheduled departure time
    val realtimeTimeIso: String?,     // ISO 8601 realtime departure (null if not available)
    val distance: Int?,               // Distance in meters (for WALK legs)
    val duration: Int,                // Duration in seconds
    val lat: Double?,                 // Latitude (for WALK legs)
    val lon: Double?,                 // Longitude (for WALK legs)
    val intermediateStops: List<String> = emptyList(), // List of intermediate stop IDs
    val tripGtfsId: String? = null,   // GTFS trip ID for real-time status checking
    val realTimeDelay: Int? = null,    // Current delay in seconds (positive = late, negative = early)
    val lastRealTimeUpdate: Long? = null // Timestamp of last real-time data update
) {
    val isWalking: Boolean
        get() = mode == "WALK"

    val transportDisplayName: String
        get() = when (mode) {
            "BUS" -> line ?: "Bus"
            "TRAM" -> line ?: "Tram"
            "RAIL" -> line ?: "Train"
            "SUBWAY" -> line ?: "Metro"
            "FERRY" -> line ?: "Ferry"
            "WALK" -> "Walk"
            else -> line ?: mode
        }

    val hasRealtimeData: Boolean
        get() = realtimeTimeIso != null

    val hasDelayInfo: Boolean
        get() = realTimeDelay != null

    val isDelayed: Boolean
        get() = realTimeDelay?.let { it > 60 } ?: false // More than 1 minute late

    val delayMinutes: Int?
        get() = realTimeDelay?.let {
            if (it > 0) (it / 60) + 1 // Round up for positive delays
            else it / 60 // Round down for early arrivals
        }

    fun getStatusText(currentTimeMillis: Long): String {
        return TimeFormatter.getStatusTextWithRealtime(
            scheduledTimeIso = scheduledTimeIso,
            realtimeTimeIso = realtimeTimeIso,
            currentTimeMillis = currentTimeMillis
        )
    }
}

@Serializable
data class Itinerary(
    val id: String,
    val legs: List<Leg>,
    val totalDuration: Int,           // Total duration in seconds
    val totalWalkingDistance: Int,    // Total walking distance in meters
    val startTimeIso: String,         // ISO 8601 start time
    val endTimeIso: String           // ISO 8601 end time
) {
    val totalMinutes: Int
        get() = totalDuration / 60

    val walkingMinutes: Int
        get() = legs.filter { it.isWalking }.sumOf { it.duration } / 60

    val hasWalking: Boolean
        get() = totalWalkingDistance > 0
}

@Serializable
data class RouteState(
    val itineraryId: String,
    val legs: List<Leg>,
    val currentIndex: Int = 0,
    val isActive: Boolean = true,
    val startTimeIso: String,
    val lastUpdated: Long = System.currentTimeMillis(),
    val fromLocation: Location? = null,
    val toLocation: Location? = null
) {
    val currentLeg: Leg?
        get() = if (currentIndex < legs.size) legs[currentIndex] else null

    val nextLeg: Leg?
        get() = if (currentIndex + 1 < legs.size) legs[currentIndex + 1] else null

    val isComplete: Boolean
        get() = currentIndex >= legs.size - 1

    val progress: Float
        get() = if (legs.isNotEmpty()) {
            (currentIndex.toFloat() / (legs.size - 1).toFloat()).coerceIn(0f, 1f)
        } else 0f

    fun moveToNextLeg(): RouteState {
        return copy(
            currentIndex = currentIndex + 1,
            isActive = currentIndex + 1 >= legs.size - 1,
            lastUpdated = System.currentTimeMillis()
        )
    }

    fun moveToPreviousLeg(): RouteState {
        return copy(
            currentIndex = (currentIndex - 1).coerceAtLeast(0),
            isActive = true,
            lastUpdated = System.currentTimeMillis()
        )
    }
}

@Serializable
data class Location(
    val id: String,                   // Stop ID or coordinates
    val name: String,                 // Human readable name (full address for search)
    val lat: Double,                  // Latitude
    val lon: Double,                  // Longitude
    val type: LocationType,          // STOP, ADDRESS, CURRENT_LOCATION
    val stopCode: String? = null,     // Stop code for transit stops
    val shortName: String? = null,    // Short name for display (e.g., "Mannerheimintie 10" vs full address)
    val lastUsed: Long = System.currentTimeMillis()  // Timestamp when location was last used
)

enum class LocationType {
    STOP, ADDRESS, CURRENT_LOCATION
}

@Serializable
data class AutocompleteResult(
    val id: String,
    val name: String,
    val lat: Double,
    val lon: Double,
    val type: LocationType,
    val stopCode: String? = null,
    val lines: List<String> = emptyList(), // List of lines serving this stop
    val shortName: String? = null // Short name for display
)

@Serializable
data class FavoriteRoute(
    val id: String,                      // Unique identifier
    val name: String,                    // User-friendly name for the route
    val fromLocation: Location,
    val toLocation: Location,
    val createdAt: Long = System.currentTimeMillis()
)