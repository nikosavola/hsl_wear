package com.hsl.wear.utils.constants

/**
 * Haptic feedback constants for Wear OS vibration patterns and durations.
 */
object HapticConstants {

    // Vibration Durations
    val HAPTIC_DURATION_LIGHT_MS = 50L     // Light haptic feedback duration
    val HAPTIC_DURATION_MEDIUM_MS = 100L    // Medium haptic feedback duration
    val HAPTIC_DURATION_STRONG_MS = 150L    // Strong haptic feedback duration

    // Vibration Patterns (array of [delay, duration, delay, duration, ...])
    val HAPTIC_PATTERN_CONFIRMATION = longArrayOf(0, HAPTIC_DURATION_LIGHT_MS, 30, HAPTIC_DURATION_LIGHT_MS)
    val HAPTIC_PATTERN_SUCCESS = longArrayOf(0, HAPTIC_DURATION_MEDIUM_MS, 50, HAPTIC_DURATION_MEDIUM_MS)
    val HAPTIC_PATTERN_WARNING = longArrayOf(0, HAPTIC_DURATION_STRONG_MS, 100, HAPTIC_DURATION_STRONG_MS)
    val HAPTIC_PATTERN_TRANSFER = longArrayOf(0, 100, 100, 100, 100, 100)
    val HAPTIC_PATTERN_DESTINATION = longArrayOf(0, 150, 100, 150, 100, 150)
    val HAPTIC_PATTERN_ERROR = longArrayOf(0, 200, 50, 200, 50, 200)
    val HAPTIC_PATTERN_NOTIFICATION = longArrayOf(0, 50, 50, 50)
    val HAPTIC_PATTERN_NAVIGATION_TURN = longArrayOf(0, 100, 30, 100)
    val HAPTIC_PATTERN_BOARDING_ALERT = longArrayOf(0, 150, 100, 150, 100, 150)
    val HAPTIC_PATTERN_ARRIVAL_ALERT = longArrayOf(0, 200, 100, 200)

    // Feedback Intensities (1-255 amplitude values)
    val HAPTIC_INTENSITY_LIGHT = 64      // 25% intensity
    val HAPTIC_INTENSITY_MEDIUM = 128     // 50% intensity
    val HAPTIC_INTENSITY_STRONG = 192     // 75% intensity
    val HAPTIC_INTENSITY_MAXIMUM = 255     // 100% intensity

    // Feedback Types and Their Patterns
    enum class HapticType(val pattern: LongArray, val intensity: Int) {
        LIGHT(longArrayOf(HAPTIC_DURATION_LIGHT_MS), HAPTIC_INTENSITY_LIGHT),
        MEDIUM(longArrayOf(HAPTIC_DURATION_MEDIUM_MS), HAPTIC_INTENSITY_MEDIUM),
        STRONG(longArrayOf(HAPTIC_DURATION_STRONG_MS), HAPTIC_INTENSITY_STRONG),
        CONFIRMATION(HAPTIC_PATTERN_CONFIRMATION, HAPTIC_INTENSITY_MEDIUM),
        SUCCESS(HAPTIC_PATTERN_SUCCESS, HAPTIC_INTENSITY_MEDIUM),
        WARNING(HAPTIC_PATTERN_WARNING, HAPTIC_INTENSITY_STRONG),
        ERROR(HAPTIC_PATTERN_ERROR, HAPTIC_INTENSITY_MAXIMUM),
        NOTIFICATION(HAPTIC_PATTERN_NOTIFICATION, HAPTIC_INTENSITY_LIGHT),
        TRANSFER(HAPTIC_PATTERN_TRANSFER, HAPTIC_INTENSITY_MEDIUM),
        DESTINATION_REACHED(HAPTIC_PATTERN_DESTINATION, HAPTIC_INTENSITY_STRONG),
        NAVIGATION_TURN(HAPTIC_PATTERN_NAVIGATION_TURN, HAPTIC_INTENSITY_MEDIUM),
        BOARDING_ALERT(HAPTIC_PATTERN_BOARDING_ALERT, HAPTIC_INTENSITY_STRONG),
        ARRIVAL_ALERT(HAPTIC_PATTERN_ARRIVAL_ALERT, HAPTIC_INTENSITY_STRONG)
    }

    // Haptic Settings
    const val HAPTIC_ENABLED_DEFAULT = true
    const val HAPTIC_INTENSITY_MULTIPLIER = 1.0f   // Can be adjusted for user preferences
    const val HAPTIC_MIN_INTERVAL_MS = 100L        // Minimum time between haptic feedbacks

    // Context-Specific Haptic Settings
    val NAVIGATION_HAPTICS_ENABLED = true
    val BUTTON_HAPTICS_ENABLED = true
    val NOTIFICATION_HAPTICS_ENABLED = true
    val ERROR_HAPTICS_ENABLED = true

    // Feedback Delays
    val HAPTIC_PREVIEW_DELAY_MS = 50L          // Delay before haptic preview
    val HAPTIC_REPETITION_DELAY_MS = 500L       // Delay between repeated haptic feedback
}