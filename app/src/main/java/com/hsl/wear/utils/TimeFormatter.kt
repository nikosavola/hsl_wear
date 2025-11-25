package com.hsl.wear.utils

import com.hsl.wear.data.models.Itinerary
import com.hsl.wear.data.models.Leg
import com.hsl.wear.data.models.RouteState
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Utility object for consistent time and duration formatting across the app.
 * Consolidates duplicate formatting logic from ViewModels and UI components.
 */
object TimeFormatter {

    /**
     * Parses ISO 8601 timestamp to epoch milliseconds.
     * @param isoTime ISO 8601 formatted string (e.g., "2024-01-15T14:30:00.000Z")
     * @return Epoch milliseconds, or current time if parsing fails
     */
    fun parseIsoTime(isoTime: String): Long {
        return try {
            Instant.parse(isoTime).toEpochMilli()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    /**
     * Formats epoch milliseconds to ISO 8601 timestamp.
     * @param epochMillis Epoch milliseconds
     * @return ISO 8601 formatted string (e.g., "2024-01-15T14:30:00+02:00")
     */
    fun formatIsoTime(epochMillis: Long): String {
        return try {
            val instant = Instant.ofEpochMilli(epochMillis)
            val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME
            instant.atZone(ZoneId.systemDefault()).format(formatter)
        } catch (e: Exception) {
            Instant.now().toString()
        }
    }

    /**
     * Formats ISO timestamp to HH:mm format.
     * @param isoTimestamp ISO 8601 formatted string
     * @return Time string in HH:mm format (e.g., "14:30")
     */
    fun formatTime(isoTimestamp: String): String {
        return try {
            // Extract time portion: "2025-11-23T14:32:00+02:00" -> "14:32"
            val timePart = isoTimestamp
                .substringAfter('T')
                .substringBefore('+')
                .substringBefore('-')
            val hourMinute = timePart.substringBefore(':') + ":" +
                            timePart.substringAfter(':').substringBefore(':')
            hourMinute
        } catch (e: Exception) {
            isoTimestamp // Fallback to original if parsing fails
        }
    }

    /**
     * Formats duration in minutes to human-readable format.
     * @param minutes Duration in minutes
     * @return Formatted string (e.g., "45 min", "2h", "1h 30m")
     */
    fun formatDuration(minutes: Int): String {
        return when {
            minutes < 60 -> "$minutes min"
            minutes % 60 == 0 -> "${minutes / 60}h"
            else -> "${minutes / 60}h ${minutes % 60}m"
        }
    }

    /**
     * Formats duration in seconds to minutes.
     * @param seconds Duration in seconds
     * @return Formatted string (e.g., "15 min")
     */
    fun formatLegDuration(seconds: Int): String {
        val minutes = seconds / 60
        return "$minutes min"
    }

    /**
     * Calculates minutes until a given time and formats status text.
     * @param isoTime ISO 8601 formatted target time
     * @param currentTimeMillis Current time in epoch milliseconds
     * @return Status text (e.g., "5min", "Now", "Departed")
     */
    fun getStatusText(isoTime: String, currentTimeMillis: Long): String {
        val targetTime = parseIsoTime(isoTime)
        val minutesUntil = ((targetTime - currentTimeMillis) / (1000 * 60)).toInt()

        return when {
            minutesUntil > 0 -> "${minutesUntil}min"
            minutesUntil == 0 -> "Now"
            else -> "Departed"
        }
    }

    /**
     * Calculates relative time with realtime data consideration.
     * @param scheduledTimeIso Scheduled departure time
     * @param realtimeTimeIso Realtime departure time (nullable)
     * @param currentTimeMillis Current time in epoch milliseconds
     * @return Status text based on realtime or scheduled time
     */
    fun getStatusTextWithRealtime(
        scheduledTimeIso: String,
        realtimeTimeIso: String?,
        currentTimeMillis: Long
    ): String {
        val timeToUse = realtimeTimeIso ?: scheduledTimeIso
        return getStatusText(timeToUse, currentTimeMillis)
    }

    /**
     * Calculates the final arrival time for a complete route (all legs).
     * @param routeState Current route state with legs
     * @return Final arrival time in epoch milliseconds
     */
    fun calculateFinalArrivalTime(routeState: RouteState): Long {
        return calculateFinalArrivalTime(routeState.legs)
    }

    /**
     * Calculates the final arrival time for a complete itinerary.
     * @param itinerary Complete itinerary with all legs
     * @return Final arrival time in epoch milliseconds
     */
    fun calculateFinalArrivalTime(itinerary: Itinerary): Long {
        return calculateFinalArrivalTime(itinerary.legs)
    }

    /**
     * Calculates the final arrival time for a list of legs.
     * @param legs List of legs in the journey
     * @return Final arrival time in epoch milliseconds
     */
    fun calculateFinalArrivalTime(legs: List<Leg>): Long {
        if (legs.isEmpty()) return System.currentTimeMillis()

        // Start with the first leg's departure time
        var currentTime = parseIsoTime(legs.first().scheduledTimeIso)

        // Add duration of each leg to get final arrival time
        legs.forEach { leg ->
            currentTime += leg.duration * 1000L
        }

        return currentTime
    }

    /**
     * Calculates the arrival time at a specific leg index.
     * @param legs List of legs in the journey
     * @param legIndex Index of the target leg
     * @return Arrival time at the specified leg in epoch milliseconds
     */
    fun calculateLegArrivalTime(legs: List<Leg>, legIndex: Int): Long {
        if (legIndex < 0 || legIndex >= legs.size) return System.currentTimeMillis()

        var currentTime = parseIsoTime(legs.first().scheduledTimeIso)

        // Add duration up to and including the target leg
        for (i in 0..legIndex) {
            currentTime += legs[i].duration * 1000L
        }

        return currentTime
    }

    /**
     * Checks if current time is within the pre-arrival window for a given arrival time.
     * @param arrivalTime Target arrival time in epoch milliseconds
     * @param currentTime Current time in epoch milliseconds
     * @param advanceMinutes Minutes before arrival to trigger notification
     * @return True if within pre-arrival window, false otherwise
     */
    fun isWithinPreArrivalWindow(arrivalTime: Long, currentTime: Long, advanceMinutes: Int): Boolean {
        val preArrivalTime = arrivalTime - (advanceMinutes * 60 * 1000L)
        return currentTime >= preArrivalTime && currentTime < arrivalTime
    }

    /**
     * Gets remaining minutes until arrival.
     * @param arrivalTime Target arrival time in epoch milliseconds
     * @param currentTime Current time in epoch milliseconds
     * @return Remaining minutes (0 if already past arrival time)
     */
    fun getRemainingMinutes(arrivalTime: Long, currentTime: Long): Int {
        val remainingMillis = arrivalTime - currentTime
        return maxOf(0, (remainingMillis / (1000 * 60)).toInt())
    }

    /**
     * Finds the next transfer leg index from the current leg.
     * @param legs List of legs in the journey
     * @param currentLegIndex Current leg index
     * @return Index of next transfer leg, or null if no transfers remaining
     */
    fun findNextTransferLegIndex(legs: List<Leg>, currentLegIndex: Int): Int? {
        // Look for the next non-walking leg after the current one
        for (i in currentLegIndex + 1 until legs.size) {
            if (legs[i].mode != "WALK") {
                return i
            }
        }
        return null
    }

    /**
     * Determines if a leg represents a transfer point (non-walking leg after walking or different transport mode).
     * @param legs List of legs in the journey
     * @param legIndex Index of the leg to check
     * @return True if this leg represents a transfer point
     */
    fun isTransferPoint(legs: List<Leg>, legIndex: Int): Boolean {
        if (legIndex <= 0 || legIndex >= legs.size) return false

        val currentLeg = legs[legIndex]
        val previousLeg = legs[legIndex - 1]

        // It's a transfer if:
        // 1. Previous leg was walking and current leg is not
        // 2. Transport mode is different between legs
        return (previousLeg.mode == "WALK" && currentLeg.mode != "WALK") ||
               (previousLeg.mode != currentLeg.mode && currentLeg.mode != "WALK")
    }
}
