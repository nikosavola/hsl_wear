package com.hsl.wear.utils

import com.hsl.wear.data.models.Itinerary
import com.hsl.wear.data.models.Leg
import com.hsl.wear.data.models.RouteState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test
import java.time.Instant

class TimeFormatterTest {

    @Test
    fun parseIsoTime_validTime_returnsMillis() {
        val isoTime = "2024-01-15T14:30:00.000Z"
        val expectedMillis = Instant.parse(isoTime).toEpochMilli()
        assertEquals(expectedMillis, TimeFormatter.parseIsoTime(isoTime))
    }

    @Test
    fun parseIsoTime_invalidTime_returnsCurrentTimeApprox() {
        val isoTime = "invalid"
        val start = System.currentTimeMillis()
        val result = TimeFormatter.parseIsoTime(isoTime)
        val end = System.currentTimeMillis()
        assertTrue("Fallback to current time failed", result in start..end)
    }

    @Test
    fun formatTime_validIso_returnsHHmm() {
        assertEquals("14:30", TimeFormatter.formatTime("2024-01-15T14:30:00.000Z"))
        assertEquals("14:32", TimeFormatter.formatTime("2025-11-23T14:32:00+02:00"))
        assertEquals("09:05", TimeFormatter.formatTime("2024-01-15T09:05:00-05:00"))
    }

    @Test
    fun formatDuration_variousMinutes_returnsCorrectString() {
        assertEquals("45 min", TimeFormatter.formatDuration(45))
        assertEquals("1h", TimeFormatter.formatDuration(60))
        assertEquals("1h 30m", TimeFormatter.formatDuration(90))
        assertEquals("2h", TimeFormatter.formatDuration(120))
    }

    @Test
    fun getStatusText_calculatesCorrectly() {
        val currentTime = Instant.parse("2024-01-15T14:00:00Z").toEpochMilli()
        assertEquals("5min", TimeFormatter.getStatusText("2024-01-15T14:05:00Z", currentTime))
        assertEquals("Now", TimeFormatter.getStatusText("2024-01-15T14:00:00Z", currentTime))
        assertEquals("Departed", TimeFormatter.getStatusText("2024-01-15T13:55:00Z", currentTime))
    }

    @Test
    fun calculateJourneyState_returnsCorrectState() {
        val departureIso = "2024-01-15T14:00:00Z" // departure
        val durationSecs = 1800 // 30 mins
        
        val beforeBoarding = Instant.parse("2024-01-15T13:50:00Z").toEpochMilli()
        assertEquals(JourneyState.BEFORE_BOARDING, TimeFormatter.calculateJourneyState(departureIso, null, beforeBoarding, durationSecs))

        val onBoard = Instant.parse("2024-01-15T14:15:00Z").toEpochMilli()
        assertEquals(JourneyState.ON_BOARD, TimeFormatter.calculateJourneyState(departureIso, null, onBoard, durationSecs))

        val arrived = Instant.parse("2024-01-15T14:35:00Z").toEpochMilli()
        assertEquals(JourneyState.ARRIVED, TimeFormatter.calculateJourneyState(departureIso, null, arrived, durationSecs))
    }
}
