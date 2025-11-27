package com.hsl.wear.utils.constants

import java.util.concurrent.TimeUnit

/**
 * Network-related constants for API endpoints, timeouts, and connection settings.
 */
object NetworkConstants {

    // HSL API Endpoints
    const val HSL_ENDPOINT = "https://api.digitransit.fi/routing/v2/hsl/gtfs/v1"
    const val HSL_ENDPOINT_V1 = "https://api.digitransit.fi/routing/v1/routers/hsl/index/graphql"

    // Geocoding Endpoints
    const val GEOCODING_ENDPOINT = "https://api.digitransit.fi/geocoding/v1/search"
    const val REVERSE_GEOCODING_ENDPOINT = "https://api.digitransit.fi/geocoding/v1/reverse"

    // Connection Timeouts
    val CONNECT_TIMEOUT_SECONDS = TimeUnit.SECONDS.toMillis(15) // 15 seconds
    val READ_TIMEOUT_SECONDS = TimeUnit.SECONDS.toMillis(30)    // 30 seconds
    val WRITE_TIMEOUT_SECONDS = TimeUnit.SECONDS.toMillis(30)    // 30 seconds

    // Route Planning Parameters
    const val TIME_ADJUSTMENT_MINUTES = 5  // 5 minutes buffer for route planning
    const val DEFAULT_NUM_ITINERARIES = 7   // Number of route options to fetch
    const val ROUTE_VALIDITY_EXTENSION_HOURS = 24  // How long to keep route valid

    // Network Retry and Error Handling
    const val MAX_RETRY_ATTEMPTS = 3
    const val RETRY_DELAY_MS = 1000  // 1 second delay between retries
    const val NETWORK_ERROR_THRESHOLD = 3  // Number of consecutive errors before showing error
}