package com.hsl.wear.utils

import com.hsl.wear.utils.constants.ZoneConstants

/**
 * Utility object for zone-related operations and formatting.
 * Provides functions for zone crossing detection, validation, and display formatting.
 */
object ZoneUtils {

    /**
     * Get all unique zones covered by a route
     */
    fun getRouteZones(fromZoneId: String?, toZoneId: String?): List<String> {
        val zones = mutableListOf<String>()

        val normalizedFromZone = ZoneConstants.normalizeZone(fromZoneId)
        val normalizedToZone = ZoneConstants.normalizeZone(toZoneId)

        normalizedFromZone?.let { zones.add(it) }
        normalizedToZone?.let { if (it != normalizedFromZone) zones.add(it) }

        return zones.distinct().sortedBy { ZoneConstants.ZONE_PRIORITY[it] ?: Int.MAX_VALUE }
    }

    /**
     * Check if a route crosses multiple zones
     */
    fun crossesMultipleZones(fromZoneId: String?, toZoneId: String?): Boolean {
        val normalizedFromZone = ZoneConstants.normalizeZone(fromZoneId)
        val normalizedToZone = ZoneConstants.normalizeZone(toZoneId)

        return normalizedFromZone != null &&
               normalizedToZone != null &&
               normalizedFromZone != normalizedToZone
    }

    /**
     * Format zone information for display in route cards
     */
    fun formatRouteZones(fromZoneId: String?, toZoneId: String?): String {
        val zones = getRouteZones(fromZoneId, toZoneId)

        return when (zones.size) {
            0 -> ""
            1 -> zones.first()
            2 -> "${zones.first()}${zones.last()}"
            else -> "${zones.first()}…${zones.last()}"
        }
    }

    /**
     * Format zone information for compact display (e.g., in navigation)
     */
    fun formatCompactZones(fromZoneId: String?, toZoneId: String?): String {
        val fromZone = ZoneConstants.getZoneDisplayName(fromZoneId)
        val toZone = ZoneConstants.getZoneDisplayName(toZoneId)

        return if (fromZone == toZone) {
            fromZone
        } else {
            "$fromZone$toZone"
        }
    }

    /**
     * Get zone transition description for navigation
     */
    fun getZoneTransitionDescription(fromZoneId: String?, toZoneId: String?): String {
        val fromZone = ZoneConstants.getZoneDisplayName(fromZoneId)
        val toZone = ZoneConstants.getZoneDisplayName(toZoneId)

        return when {
            fromZone == "?" && toZone != "?" -> "Zone $toZone"
            fromZone != "?" && toZone == "?" -> "Zone $fromZone"
            fromZone == toZone -> "Zone $fromZone"
            else -> "Zone $fromZone→$toZone"
        }
    }

    /**
     * Check if a transition occurs between zones
     */
    fun hasZoneTransition(fromZoneId: String?, toZoneId: String?): Boolean {
        val normalizedFromZone = ZoneConstants.normalizeZone(fromZoneId)
        val normalizedToZone = ZoneConstants.normalizeZone(toZoneId)

        return normalizedFromZone != null &&
               normalizedToZone != null &&
               normalizedFromZone != normalizedToZone
    }

    /**
     * Get zone indicator for a single stop (used in location search)
     */
    fun getStopZoneIndicator(zoneId: String?): String {
        return ZoneConstants.getZoneDisplayName(zoneId)
    }
}