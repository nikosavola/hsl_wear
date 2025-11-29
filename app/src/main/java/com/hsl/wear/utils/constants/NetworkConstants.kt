package com.hsl.wear.utils.constants

import java.util.concurrent.TimeUnit

/**
 * Network-related constants for API endpoints, timeouts, and connection settings.
 */
object NetworkConstants {

    // HSL API Endpoints
    const val HSL_ENDPOINT = "https://api.digitransit.fi/routing/v2/hsl/gtfs/v1"

    // Geocoding Endpoints
    const val GEOCODING_ENDPOINT = "https://api.digitransit.fi/geocoding/v1/search"
    const val REVERSE_GEOCODING_ENDPOINT = "https://api.digitransit.fi/geocoding/v1/reverse"

    // Route Planning Parameters
    const val TIME_ADJUSTMENT_MINUTES = 5  // 5 minutes buffer for route planning
    const val DEFAULT_NUM_ITINERARIES = 7   // Number of route options to fetch
}