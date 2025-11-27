package com.hsl.wear.utils.constants

/**
 * Time-related constants for timeouts, delays, intervals, and durations.
 */
object TimeConstants {

    // Wear OS Tile Constants
    val TILE_FRESHNESS_INTERVAL_MS = 300_000L      // 5 minutes in milliseconds
    val TILE_ROUTE_VALIDITY_START_ADJUSTMENT_MS = 60_000L  // 1 minute before route validity
    val TILE_ROUTE_CLEANUP_DELAY_MS = 120_000L      // 2 minutes after arrival for cleanup

    // Animation Durations
    val BUTTON_ANIMATION_DURATION_MS = 200L         // Button press animation
    val HAPTIC_DURATION_LIGHT_MS = 50L              // Light haptic feedback
    val HAPTIC_DURATION_MEDIUM_MS = 100L             // Medium haptic feedback
    val HAPTIC_DURATION_STRONG_MS = 150L             // Strong haptic feedback

    // Network and API Timeouts
    val CONNECTION_TIMEOUT_MS = 15_000L               // 15 seconds
    val READ_TIMEOUT_MS = 30_000L                     // 30 seconds
    val WRITE_TIMEOUT_MS = 30_000L                     // 30 seconds

    // Route Planning Time
    val TIME_BUFFER_MINUTES = 5                        // 5 minutes buffer for planning
    val ROUTE_VALIDITY_HOURS = 24                     // 24 hours route validity

    // Update Intervals
    val LOCATION_UPDATE_INTERVAL_MS = 10_000L          // 10 seconds location updates
    val UI_REFRESH_INTERVAL_MS = 5_000L                // 5 seconds UI refresh

    // Debounce and Delays
    val SEARCH_DEBOUNCE_MS = 300L                     // 300ms debounce for search
    val BUTTON_DEBOUNCE_MS = 500L                     // 500ms debounce for buttons
    val NAVIGATION_UPDATE_DELAY_MS = 1_000L           // 1 second delay between navigation updates

    // Time Display Formatters
    val MINUTES_IN_HOUR = 60
    val SECONDS_IN_MINUTE = 60
    val MILLISECONDS_IN_SECOND = 1000
    val MILLISECONDS_IN_MINUTE = 60_000
    val MILLISECONDS_IN_HOUR = 3_600_000

    // Countdown Thresholds
    val URGENT_COUNTDOWN_THRESHOLD_MINUTES = 2        // When to show urgent countdown
    val WARNING_COUNTDOWN_THRESHOLD_MINUTES = 5        // When to show warning state
    val NORMAL_COUNTDOWN_THRESHOLD_MINUTES = 10        // Normal countdown display

    // Navigation Timing
    val TRANSFER_ALERT_TIME_MS = 2_000L               // 2 seconds before transfer alert
    val ARRIVAL_ALERT_TIME_MS = 1_000L                 // 1 second before arrival alert
    val BOARDING_ALERT_TIME_MS = 30_000L               // 30 seconds before boarding alert

    // Cache Times
    val LOCATION_CACHE_DURATION_MS = 5_000L            // 5 seconds location cache
    val ROUTE_CACHE_DURATION_MS = 30_000L              // 30 seconds route cache
    val FAVORITES_CACHE_DURATION_MS = 300_000L         // 5 minutes favorites cache
}