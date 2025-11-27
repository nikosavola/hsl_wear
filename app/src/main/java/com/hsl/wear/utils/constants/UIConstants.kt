package com.hsl.wear.utils.constants

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * UI-related constants for dimensions, spacing, text sizes, and layout values.
 */
object UIConstants {

    // Button Dimensions
    val BUTTON_HEIGHT = 52.dp
    val BUTTON_CORNER_RADIUS = 26.dp
    val BUTTON_SCALE_PRESSED = 0.95f
    val BUTTON_LETTER_SPACING = 0.1.sp

    // Chip Components
    val CHIP_HEIGHT = 40.dp
    val CHIP_CORNER_RADIUS = 20.dp
    val CHIP_HORIZONTAL_PADDING = 12.dp
    val CHIP_VERTICAL_PADDING = 6.dp
    val CHIP_ICON_SIZE = 16.dp

    // Icon Sizes
    val DEFAULT_ICON_SIZE = 24.dp
    val SMALL_ICON_SIZE = 16.dp
    val PROGRESS_INDICATOR_SIZE = 16.dp
    val PROGRESS_INDICATOR_STROKE_WIDTH = 2.dp

    // Spacing Values
    val SPACING_SMALL = 4.dp
    val SPACING_MEDIUM = 8.dp
    val SPACING_LARGE = 16.dp
    val SPACING_EXTRA_LARGE = 24.dp

    // Padding Values
    val PADDING_SMALL = 4.dp
    val PADDING_MEDIUM = 8.dp
    val PADDING_LARGE = 12.dp
    val PADDING_HORIZONTAL_DEFAULT = 8.dp
    val PADDING_VERTICAL_DEFAULT = 4.dp

    // Text Sizes
    val TEXT_SIZE_SMALL = 12.sp
    val TEXT_SIZE_MEDIUM = 15.sp
    val TEXT_SIZE_LABEL = 15.sp
    val TEXT_SIZE_TITLE = 16.sp
    val TEXT_SIZE_NAVIGATION = 17.sp
    val TEXT_SIZE_LARGE = 18.sp
    val TEXT_SIZE_TILE = 20.sp
    val TEXT_SIZE_COUNTDOWN = 18.sp

    // Wear OS Tile Specific
    val TILE_WIDTH = 120f
    val TILE_NAVIGATION_BUTTON_HEIGHT = 30f
    val TILE_NAVIGATION_BUTTON_WIDTH = 60f
    val TILE_NAVIGATION_TEXT_SIZE = 16f
    val TILE_STATION_NAME_TEXT_SIZE = 17f
    val TILE_STATUS_TEXT_SIZE = 15f
    val TILE_TRANSPORT_TEXT_SIZE = 18f

    // Layout Constraints for Tile Service
    val TILE_STATUS_LAYOUT_CONSTRAINT = "Platform 99        "
    val TILE_STATION_LAYOUT_CONSTRAINT = "Helsinki Central Railway\nStation Platform 1"
    val TILE_COUNTDOWN_LAYOUT_CONSTRAINT = "Arrives in 999 min"

    // Animation Constants
    val ANIMATION_DURATION_MS = 200  // Button animation duration
    val ANIMATION_EASING = androidx.compose.animation.core.EaseOutCubic

    // Card and Container Dimensions
    val CARD_CORNER_RADIUS = 8.dp
    val CARD_HORIZONTAL_PADDING = 12.dp
    val CARD_VERTICAL_PADDING = 8.dp
}