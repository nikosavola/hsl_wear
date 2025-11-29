package com.hsl.wear.utils.constants

/**
 * Time-related constants for timeouts, delays, intervals, and durations.
 */
object TimeConstants {
    val REALTIME_UPDATE_INTERVAL_MS = 30_000L           // 30 seconds real-time updates
    val REALTIME_ERROR_RETRY_INTERVAL_MS = 60_000L      // 60 seconds retry on error

    // Debounce and Delays
    val SEARCH_DEBOUNCE_MS = 300L                     // 300ms debounce for search
    val BUTTON_DEBOUNCE_MS = 500L                     // 500ms debounce for buttons
    val FEEDBACK_RESET_DELAY_MS = 2_000L              // 2 seconds delay to reset feedback flags

    // Time Display Formatters
    val MINUTES_IN_HOUR = 60
    val SECONDS_IN_MINUTE = 60
    val MILLISECONDS_IN_SECOND = 1000
    val MILLISECONDS_IN_MINUTE = 60_000
    val MILLISECONDS_IN_HOUR = 3_600_000
}