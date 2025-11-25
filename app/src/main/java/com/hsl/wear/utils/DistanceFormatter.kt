package com.hsl.wear.utils

/**
 * Utility object for consistent distance formatting across the app.
 */
object DistanceFormatter {

    /**
     * Formats distance in meters to human-readable format.
     * @param meters Distance in meters
     * @return Formatted string (e.g., "250 m", "1.5 km")
     */
    fun formatDistance(meters: Int): String {
        return when {
            meters < 1000 -> "$meters m"
            else -> String.format("%.1f km", meters / 1000.0)
        }
    }

    /**
     * Formats walking distance in compact format.
     * @param meters Distance in meters
     * @return Formatted string (e.g., "250m", "1.5km")
     */
    fun formatWalkingDistance(meters: Int): String {
        return when {
            meters < 1000 -> "${meters}m"
            else -> {
                val km = meters / 1000.0
                String.format("%.1fkm", km)
            }
        }
    }
}
