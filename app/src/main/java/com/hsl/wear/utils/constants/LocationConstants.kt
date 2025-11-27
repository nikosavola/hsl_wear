package com.hsl.wear.utils.constants

/**
 * Location-related constants for search thresholds, scoring, and geographic calculations.
 */
object LocationConstants {

    // Location Scoring Weights
    const val QUERY_RELEVANCE_WEIGHT = 40
    const val LOCATION_TYPE_BONUS_WEIGHT = 20
    const val FAVORITE_LOCATION_BONUS_WEIGHT = 25
    const val RECENT_LOCATION_BONUS_WEIGHT = 15
    const val PROXIMITY_BONUS_WEIGHT = 20
    const val TRANSPORT_LINES_BONUS_WEIGHT = 10

    // Location Type Bonus Points
    const val STOP_TYPE_BONUS_POINTS = 20
    const val ADDRESS_TYPE_BONUS_POINTS = 15
    const val DEFAULT_TYPE_BONUS_POINTS = 5

    // Proximity Thresholds (in kilometers)
    const val PROXIMITY_THRESHOLD_VERY_CLOSE_KM = 0.5    // 500 meters
    const val PROXIMITY_THRESHOLD_CLOSE_KM = 1.0           // 1 kilometer
    const val PROXIMITY_THRESHOLD_MODERATE_KM = 2.0        // 2 kilometers
    const val PROXIMITY_THRESHOLD_FAR_KM = 5.0             // 5 kilometers

    // Transport Lines Count Thresholds
    const val TRANSPORT_LINES_HIGH_COUNT = 10
    const val TRANSPORT_LINES_MEDIUM_COUNT = 5
    const val TRANSPORT_LINES_LOW_COUNT = 3
    const val TRANSPORT_LINES_MIN_COUNT = 1

    // Search Result Limits
    const val MAX_SEARCH_RESULTS = 10
    const val MAX_GEOCODING_RESULTS = 15
    const val MAX_REVERSE_GEOCODING_RESULTS = 5

    // Location Matching Thresholds
    const val LOCATION_MATCHING_THRESHOLD = 0.001         // For location similarity matching
    const val NAME_SIMILARITY_THRESHOLD = 0.8            // For name similarity matching

    // Recent Location Time Thresholds (in hours)
    const val RECENT_LOCATION_HOUR_THRESHOLD_1 = 1         // Within 1 hour
    const val RECENT_LOCATION_HOUR_THRESHOLD_6 = 6         // Within 6 hours
    const val RECENT_LOCATION_HOUR_THRESHOLD_24 = 24        // Within 1 day
    const val RECENT_LOCATION_HOUR_THRESHOLD_168 = 168      // Within 1 week

    // Geographic Constants
    const val EARTH_RADIUS_KM = 6371                      // Earth's radius in kilometers for distance calculations
    const val METERS_IN_KILOMETER = 1000
    const val FEET_IN_METER = 3.28084

    // Distance Display Constants
    const val DISTANCE_THRESHOLD_METERS = 1000              // Switch to km display above this
    const val WALKING_SPEED_METERS_PER_MINUTE = 80         // Average walking speed
    const val CYCLING_SPEED_METERS_PER_MINUTE = 250        // Average cycling speed

    // Location Update Intervals
    const val LOCATION_UPDATE_INTERVAL_MS = 10_000L          // 10 seconds
    const val LOCATION_FAST_UPDATE_INTERVAL_MS = 5_000L      // 5 seconds during navigation
    const val LOCATION_SLOW_UPDATE_INTERVAL_MS = 30_000L     // 30 seconds in background

    // Accuracy Thresholds
    const val GPS_ACCURACY_THRESHOLD_EXCELLENT = 5.0f      // 5 meters
    const val GPS_ACCURACY_THRESHOLD_GOOD = 10.0f         // 10 meters
    const val GPS_ACCURACY_THRESHOLD_FAIR = 20.0f         // 20 meters
    const val GPS_ACCURACY_THRESHOLD_POOR = 50.0f         // 50 meters

    // Location Cache Constants
    const val LOCATION_CACHE_MAX_SIZE = 50                  // Maximum cached locations
    const val LOCATION_CACHE_EXPIRY_MS = 300_000L           // 5 minutes cache expiry

    // Favorite Location Constants
    const val MAX_FAVORITE_LOCATIONS = 10                   // Maximum favorite routes a user can save
    const val FAVORITE_LOCATION_DEFAULT_NAME = "Home"      // Default name for favorite location
}