package com.hsl.wear.utils

/**
 * Centralized error messages for consistent user feedback across the app.
 */
object ErrorMessages {

    // Location-related errors
    const val LOCATION_PERMISSION_DENIED = "Location permission not granted. Please enable location in settings."
    const val LOCATION_UNAVAILABLE = "Could not get current location. Make sure location is enabled."
    const val LOCATION_SEARCH_FAILED = "Failed to search locations"
    const val REVERSE_GEOCODE_FAILED = "Could not find address for current location"

    // Route planning errors
    const val ROUTE_PLANNING_FAILED = "Failed to plan route"
    const val MISSING_LOCATIONS = "Please select both from and to locations"
    const val NO_ROUTES_FOUND = "No routes found between selected locations"

    // Favorite route errors
    const val FAVORITE_SAVE_FAILED = "Failed to save favorite"
    const val FAVORITE_MISSING_INFO = "Cannot save route: missing location information"
    const val FAVORITE_DELETE_FAILED = "Failed to delete favorite"

    // Network errors
    const val NETWORK_ERROR = "Network error occurred. Please check your connection."
    const val API_ERROR = "Failed to communicate with HSL API"
    const val TIMEOUT_ERROR = "Request timed out. Please try again."

    /**
     * Formats an error message with exception details.
     * @param baseMessage The base error message
     * @param exception The exception that occurred
     * @return Formatted error message
     */
    fun withException(baseMessage: String, exception: Throwable?): String {
        return if (exception?.message != null) {
            "$baseMessage: ${exception.message}"
        } else {
            baseMessage
        }
    }

    /**
     * Gets a user-friendly error message from an exception.
     * @param exception The exception
     * @param defaultMessage Default message if exception type is unknown
     * @return User-friendly error message
     */
    fun fromException(exception: Throwable, defaultMessage: String = "An error occurred"): String {
        return when (exception) {
            is java.net.UnknownHostException -> NETWORK_ERROR
            is java.net.SocketTimeoutException -> TIMEOUT_ERROR
            is java.io.IOException -> NETWORK_ERROR
            else -> exception.message ?: defaultMessage
        }
    }
}
