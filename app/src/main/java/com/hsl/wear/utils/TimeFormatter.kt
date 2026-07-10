package com.hsl.wear.utils

import com.hsl.wear.data.models.Itinerary
import com.hsl.wear.data.models.Leg
import com.hsl.wear.data.models.RouteState
import com.hsl.wear.utils.constants.TimeConstants
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Represents the current state of a leg in a journey.
 */
enum class JourneyState {
    BEFORE_BOARDING,  // Before departure time
    ON_BOARD,        // After departure but before arrival
    ARRIVED          // After arrival time
}

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
    fun parseIsoTime(isoTime: String): Long? {
        return try {
            OffsetDateTime.parse(isoTime).toInstant().toEpochMilli()
        } catch (e: Exception) {
            try {
                Instant.parse(isoTime).toEpochMilli()
            } catch (e2: Exception) {
                null
            }
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
            val millis = parseIsoTime(isoTimestamp) ?: return isoTimestamp
            val instant = Instant.ofEpochMilli(millis)
            val formatter = DateTimeFormatter.ofPattern("HH:mm")
            instant.atZone(ZoneId.systemDefault()).format(formatter)
        } catch (e: Exception) {
            isoTimestamp
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
        val targetTime = parseIsoTime(isoTime) ?: return "Error"
        val minutesUntil = ((targetTime - currentTimeMillis) / (TimeConstants.MILLISECONDS_IN_SECOND * TimeConstants.SECONDS_IN_MINUTE)).toInt()

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
    fun calculateFinalArrivalTime(routeState: RouteState): Long? {
        return calculateFinalArrivalTime(routeState.legs)
    }

    /**
     * Calculates the final arrival time for a complete itinerary.
     * @param itinerary Complete itinerary with all legs
     * @return Final arrival time in epoch milliseconds
     */
    fun calculateFinalArrivalTime(itinerary: Itinerary): Long? {
        return calculateFinalArrivalTime(itinerary.legs)
    }

    /**
     * Calculates the final arrival time for a list of legs.
     * @param legs List of legs in the journey
     * @return Final arrival time in epoch milliseconds
     */
    fun calculateFinalArrivalTime(legs: List<Leg>): Long? {
        if (legs.isEmpty()) return null

        var currentTime = parseIsoTime(legs.first().scheduledTimeIso) ?: return null

        legs.forEach { leg ->
            currentTime += leg.duration * TimeConstants.MILLISECONDS_IN_SECOND
        }

        return currentTime
    }

    /**
     * Calculates the arrival time at a specific leg index.
     * @param legs List of legs in the journey
     * @param legIndex Index of the target leg
     * @return Arrival time at the specified leg in epoch milliseconds
     */
    fun calculateLegArrivalTime(legs: List<Leg>, legIndex: Int): Long? {
        if (legIndex < 0 || legIndex >= legs.size) return null

        var currentTime = parseIsoTime(legs.first().scheduledTimeIso) ?: return null

        for (i in 0..legIndex) {
            currentTime += legs[i].duration * TimeConstants.MILLISECONDS_IN_SECOND
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
        val preArrivalTime = arrivalTime - (advanceMinutes * TimeConstants.SECONDS_IN_MINUTE * TimeConstants.MILLISECONDS_IN_SECOND)
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

    /**
     * Calculates the current journey state for a leg.
     * @param scheduledTimeIso Scheduled departure time in ISO format
     * @param realtimeTimeIso Realtime departure time in ISO format (optional)
     * @param currentTimeMillis Current time in epoch milliseconds
     * @param duration Leg duration in seconds
     * @return JourneyState indicating the current phase of the leg
     */
    fun calculateJourneyState(
        scheduledTimeIso: String,
        realtimeTimeIso: String?,
        currentTimeMillis: Long,
        duration: Int
    ): JourneyState {
        val departureTime = parseIsoTime(realtimeTimeIso ?: scheduledTimeIso) ?: return JourneyState.BEFORE_BOARDING
        val arrivalTime = departureTime + (duration * 1000)

        return when {
            currentTimeMillis < departureTime -> JourneyState.BEFORE_BOARDING
            currentTimeMillis >= arrivalTime -> JourneyState.ARRIVED
            else -> JourneyState.ON_BOARD
        }
    }

    /**
     * Calculates the current journey state for a Leg object.
     * @param leg Leg to calculate state for
     * @param currentTimeMillis Current time in epoch milliseconds
     * @return JourneyState indicating the current phase of the leg
     */
    fun calculateJourneyState(leg: Leg, currentTimeMillis: Long): JourneyState {
        return calculateJourneyState(
            leg.scheduledTimeIso,
            leg.realtimeTimeIso,
            currentTimeMillis,
            leg.duration
        )
    }

    /**
     * Calculates remaining time until departure.
     * @param scheduledTimeIso Scheduled departure time in ISO format
     * @param realtimeTimeIso Realtime departure time in ISO format (optional)
     * @param currentTimeMillis Current time in epoch milliseconds
     * @return Minutes until departure (negative if already departed)
     */
    fun getTimeUntilDeparture(
        scheduledTimeIso: String,
        realtimeTimeIso: String?,
        currentTimeMillis: Long
    ): Int {
        val departureTime = parseIsoTime(realtimeTimeIso ?: scheduledTimeIso) ?: return 0
        return ((departureTime - currentTimeMillis) / (1000 * 60)).toInt()
    }

    /**
     * Calculates remaining time until departure for a Leg object.
     * @param leg Leg to calculate departure time for
     * @param currentTimeMillis Current time in epoch milliseconds
     * @return Minutes until departure (negative if already departed)
     */
    fun getTimeUntilDeparture(leg: Leg, currentTimeMillis: Long): Int {
        return getTimeUntilDeparture(
            leg.scheduledTimeIso,
            leg.realtimeTimeIso,
            currentTimeMillis
        )
    }

    /**
     * Calculates remaining time until arrival.
     * @param scheduledTimeIso Scheduled departure time in ISO format
     * @param realtimeTimeIso Realtime departure time in ISO format (optional)
     * @param currentTimeMillis Current time in epoch milliseconds
     * @param duration Leg duration in seconds
     * @return Minutes until arrival (negative if already arrived)
     */
    fun getTimeUntilArrival(
        scheduledTimeIso: String,
        realtimeTimeIso: String?,
        currentTimeMillis: Long,
        duration: Int
    ): Int {
        val departureTime = parseIsoTime(realtimeTimeIso ?: scheduledTimeIso) ?: return 0
        val arrivalTime = departureTime + (duration * 1000)
        return ((arrivalTime - currentTimeMillis) / (1000 * 60)).toInt()
    }

    /**
     * Calculates remaining time until arrival for a Leg object.
     * @param leg Leg to calculate arrival time for
     * @param currentTimeMillis Current time in epoch milliseconds
     * @return Minutes until arrival (negative if already arrived)
     */
    fun getTimeUntilArrival(leg: Leg, currentTimeMillis: Long): Int {
        return getTimeUntilArrival(
            leg.scheduledTimeIso,
            leg.realtimeTimeIso,
            currentTimeMillis,
            leg.duration
        )
    }
}
