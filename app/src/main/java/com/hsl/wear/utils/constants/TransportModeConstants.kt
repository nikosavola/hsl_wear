package com.hsl.wear.utils.constants

/**
 * Transport mode constants for consistent handling of different transit types.
 * Provides standardized strings and mappings for all supported transport modes.
 */
object TransportModeConstants {

    // Transport mode strings used in HSL API
    const val BUS = "BUS"
    const val TRAM = "TRAM"
    const val RAIL = "RAIL"
    const val FERRY = "FERRY"
    const val WALK = "WALK"

    // Metro is sometimes used as a separate mode
    const val METRO = "METRO"

    /**
     * Maps API transport modes to display names.
     * This can be used for consistent display throughout the app.
     */
    fun getDisplayName(mode: String): String = when (mode) {
        BUS -> "Bus"
        TRAM -> "Tram"
        RAIL -> "Train"
        FERRY -> "Ferry"
        WALK -> "Walk"
        METRO -> "Metro"
        else -> mode // Fallback to original string
    }

    /**
     * Gets all supported transport modes.
     */
    fun getAllModes(): List<String> = listOf(
        BUS, TRAM, RAIL, FERRY, WALK, METRO
    )

    /**
     * Checks if a transport mode is a motorized transit mode.
     * @return true if the mode requires a vehicle (not walking)
     */
    fun isMotorizedTransit(mode: String): Boolean = when (mode) {
        WALK -> false
        else -> true
    }

    /**
     * Checks if a transport mode is public transit.
     * @return true if the mode is public transportation
     */
    fun isPublicTransit(mode: String): Boolean = when (mode) {
        WALK -> false
        BUS, TRAM, RAIL, FERRY, METRO -> true
        else -> false
    }
}