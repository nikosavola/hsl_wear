package com.hsl.wear.utils.constants

import androidx.compose.ui.graphics.Color

/**
 * Wear OS Tile-specific constants for layout, dimensions, and content.
 */
object TileConstants {

    // Tile Dimensions and Layout
    val TILE_WIDTH = 120f
    val TILE_NAVIGATION_BUTTON_HEIGHT = 30f
    val TILE_NAVIGATION_BUTTON_WIDTH = 60f
    val TILE_NAVIGATION_TEXT_SIZE = 16f
    val TILE_TRANSPORT_MODE_TEXT_SIZE = 18f
    val TILE_NO_ACTIVE_ROUTE_TEXT_SIZE = 20f
    val TILE_STATUS_TEXT_SIZE = 15f
    val TILE_STATION_NAME_TEXT_SIZE = 17f
    val TILE_COUNTDOWN_TEXT_SIZE = 18f

    // Tile Text Content
    const val TILE_NO_ACTIVE_ROUTE_TEXT = "No Active"
    const val TILE_NO_ACTIVE_SUBTITLE_TEXT = "Route"
    const val TILE_REFRESH_INSTRUCTION = "Tap ↻ to refresh"

    // Tile Layout Constraints (used for text measurement)
    const val TILE_STATUS_LAYOUT_CONSTRAINT = "Platform 99        "
    const val TILE_STATION_LAYOUT_CONSTRAINT = "Helsinki Central Railway\nStation Platform 1"
    const val TILE_COUNTDOWN_LAYOUT_CONSTRAINT = "Arrives in 999 min"

    // Tile Update Intervals
    val TILE_FRESHNESS_INTERVAL_MS = 300_000L        // 5 minutes
    val TILE_ROUTE_VALIDITY_START_ADJUSTMENT_MS = 60_000L  // 1 minute before route validity
    val TILE_ROUTE_CLEANUP_DELAY_MS = 120_000L        // 2 minutes after arrival
    val TILE_UPDATE_INTERVAL_MS = 60_000L              // 1 minute (for active route updates)

    // Tile Content Constants
    const val TILE_MAX_TRANSPORT_MODE_LENGTH = 20        // Max characters for transport mode display
    const val TILE_MAX_STATION_NAME_LENGTH = 30         // Max characters for station name
    const val TILE_MAX_HEADSIGN_LENGTH = 20             // Max characters for headsign

    // Tile Button Configuration
    val TILE_BUTTON_ICON_SIZE = 24f
    val TILE_BUTTON_SPACING = 8f
    val TILE_BUTTON_MARGIN = 4f

    // Tile Colors
    val TILE_BACKGROUND_COLOR = Color.Black
    val TILE_TEXT_COLOR = Color.White
    val TILE_ACCENT_COLOR = Color(0xFF007AC9)  // HSL Blue
    val TILE_BUTTON_COLOR = Color.Gray
    val TILE_TRANSPORT_MODE_COLOR = Color.White

    // Tile Text Prefixes and Suffixes
    const val TILE_PLATFORM_PREFIX = "Platform "
    const val TILE_DIRECTION_PREFIX = "→ "
    const val TILE_ARRIVES_IN_PREFIX = "Arrives in "
    const val TILE_BOARDS_IN_PREFIX = "Boards in "
    const val TILE_MINUTES_SUFFIX = " min"

    // Tile State Messages
    const val TILE_ON_BOARD_TEXT = "On board"
    const val TILE_WALKING_TEXT = "Walking"
    const val TILE_WAITING_TEXT = "Waiting"
    const val TILE_TRANSFER_TEXT = "Transfer"
    const val TILE_ARRIVING_NOW_TEXT = "Arriving now"
    const val TILE_BOARDING_NOW_TEXT = "Boarding now"
    const val TILE_DEPARTED_TEXT = "Departed"

    // Tile Ferry Service Constants
    const val TILE_FERRY_SERVICE_TEXT = "Ferry service"
    val TILE_FERRY_ICON_SIZE = 18f

    // Tile Performance Constants
    val TILE_RENDER_TIMEOUT_MS = 5000L                // 5 seconds max render time
    val TILE_MAX_BITMAP_SIZE = 512                     // Maximum bitmap size for tile
    const val TILE_CACHE_SIZE = 5                      // Number of tiles to cache

    // Tile Accessibility Constants
    const val TILE_CONTENT_DESCRIPTION_PREFIX = "Current transit information"
    const val TILE_REFRESH_CONTENT_DESCRIPTION = "Refresh current route information"
    const val TILE_PREVIOUS_CONTENT_DESCRIPTION = "Show previous leg"
    const val TILE_NEXT_CONTENT_DESCRIPTION = "Show next leg"

    // Tile Refresh Controls
    const val TILE_REFRESH_ICON = "↻"
    const val TILE_PREVIOUS_ICON = "◀"
    const val TILE_NEXT_ICON = "▶"
    val TILE_ICON_SIZE = 16f

    // Tile Error States
    const val TILE_ERROR_NO_ROUTE_TEXT = "No active route"
    const val TILE_ERROR_NETWORK_TEXT = "Network error"
    const val TILE_ERROR_LOCATION_TEXT = "Location unavailable"
    const val TILE_ERROR_REFRESH_TEXT = "Failed to refresh"

    // Tile Debug Constants (remove in production)
    const val TILE_DEBUG_ENABLED = false
    const val TILE_DEBUG_PREFIX = "[DEBUG] "
    const val TILE_DEBUG_TIMESTAMP_FORMAT = "HH:mm:ss"
}