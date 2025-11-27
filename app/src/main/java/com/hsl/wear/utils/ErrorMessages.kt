package com.hsl.wear.utils

import android.content.Context
import com.hsl.wear.R

/**
 * Centralized error messages for consistent user feedback across the app.
 * This class requires a Context to access string resources.
 */
class ErrorMessages(private val context: Context) {

    companion object {
        // Keep the fromException method as a companion for convenience
        /**
         * Gets a user-friendly error message from an exception.
         * @param exception The exception
         * @param defaultMessage Default message if exception type is unknown
         * @return User-friendly error message
         */
        fun fromException(exception: Throwable, defaultMessage: String = "An error occurred"): String {
            return when (exception) {
                is java.net.UnknownHostException -> "Network error occurred. Please check your connection."
                is java.net.SocketTimeoutException -> "Request timed out. Please try again."
                is java.io.IOException -> "Network error occurred. Please check your connection."
                else -> exception.message ?: defaultMessage
            }
        }
    }

    // Location-related errors
    fun getLocationPermissionDenied(): String = context.getString(R.string.location_permission_denied)
    fun getLocationUnavailable(): String = context.getString(R.string.error_location_not_enabled)
    fun getLocationSearchFailed(): String = context.getString(R.string.error_search_locations)
    fun getReverseGeocodeFailed(): String = context.getString(R.string.error_current_location_address)

    // Route planning errors
    fun getRoutePlanningFailed(): String = context.getString(R.string.error_route_planning)
    fun getMissingLocations(): String = context.getString(R.string.error_missing_locations)
    fun getNoRoutesFound(): String = context.getString(R.string.error_no_routes)

    // Favorite route errors
    fun getFavoriteSaveFailed(): String = context.getString(R.string.error_save_favorite)
    fun getFavoriteMissingInfo(): String = context.getString(R.string.error_missing_route_info)
    fun getFavoriteDeleteFailed(): String = context.getString(R.string.error_delete_favorite)

    // Network errors
    fun getNetworkError(): String = context.getString(R.string.network_error)
    fun getApiError(): String = context.getString(R.string.error_hsl_api)
    fun getTimeoutError(): String = context.getString(R.string.error_request_timeout)

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
     * Gets a user-friendly error message from an exception using string resources.
     * @param exception The exception
     * @param defaultMessage Default message if exception type is unknown
     * @return User-friendly error message
     */
    fun fromExceptionWithContext(exception: Throwable, defaultMessage: String = "An error occurred"): String {
        return when (exception) {
            is java.net.UnknownHostException -> getNetworkError()
            is java.net.SocketTimeoutException -> getTimeoutError()
            is java.io.IOException -> getNetworkError()
            else -> exception.message ?: defaultMessage
        }
    }
}
