package com.hsl.wear.utils.constants

/**
 * HSL fare zone constants for consistent zone handling across the app.
 * Provides standardized zone identifiers and display information.
 */
object ZoneConstants {

    // HSL fare zone identifiers
    const val ZONE_A = "A"
    const val ZONE_B = "B"
    const val ZONE_C = "C"
    const val ZONE_D = "D"

    // All valid zones
    val ALL_ZONES = listOf(ZONE_A, ZONE_B, ZONE_C, ZONE_D)

    // Zone display names (can be extended for localization)
    val ZONE_DISPLAY_NAMES = mapOf(
        ZONE_A to "A",
        ZONE_B to "B",
        ZONE_C to "C",
        ZONE_D to "D"
    )

    // Zone priority order for sorting (A > B > C > D)
    val ZONE_PRIORITY = mapOf(
        ZONE_A to 1,
        ZONE_B to 2,
        ZONE_C to 3,
        ZONE_D to 4
    )

    /**
     * Check if a zone identifier is valid
     */
    fun isValidZone(zone: String?): Boolean {
        return zone != null && ALL_ZONES.contains(zone.uppercase())
    }

    /**
     * Normalize zone identifier to uppercase
     */
    fun normalizeZone(zone: String?): String? {
        return zone?.uppercase()?.takeIf { isValidZone(it) }
    }

    /**
     * Get display name for zone
     */
    fun getZoneDisplayName(zone: String?): String {
        val normalizedZone = normalizeZone(zone)
        return normalizedZone?.let { ZONE_DISPLAY_NAMES[it] } ?: "?"
    }
}