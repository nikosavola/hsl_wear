package com.hsl.wear.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Instant

class TimeFormatterTest {

    @Test
    fun parseIsoTime_validTimestamp() {
        val time = "2025-11-23T14:32:00Z"
        val expected = Instant.parse(time).toEpochMilli()
        assertEquals(expected, TimeFormatter.parseIsoTime(time))
    }

    @Test
    fun parseIsoTime_withOffset() {
        val time = "2025-11-23T14:32:00-02:00"
        val expected = Instant.parse("2025-11-23T16:32:00Z").toEpochMilli()
        assertEquals(expected, TimeFormatter.parseIsoTime(time))
    }

    @Test
    fun parseIsoTime_withFractionalSeconds() {
        val time = "2025-11-23T14:32:00.123Z"
        val expected = Instant.parse(time).toEpochMilli()
        assertEquals(expected, TimeFormatter.parseIsoTime(time))
    }

    @Test
    fun parseIsoTime_garbageInput() {
        assertNull(TimeFormatter.parseIsoTime("not-a-timestamp"))
        assertNull(TimeFormatter.parseIsoTime(""))
        assertNull(TimeFormatter.parseIsoTime("2025-11-23 14:32:00"))
    }

    @Test
    fun formatTime_validIso() {
        // It converts to system timezone. Just ensure it parses without slicing wrongly.
        val result = TimeFormatter.formatTime("2025-11-23T14:32:00Z")
        assert(result.matches(Regex("\\d{2}:\\d{2}")))
    }

    @Test
    fun formatTime_withOffset() {
        val result = TimeFormatter.formatTime("2025-11-23T14:32:00-02:00")
        assert(result.matches(Regex("\\d{2}:\\d{2}")))
    }

    @Test
    fun formatTime_garbageInput() {
        val garbage = "invalid time"
        assertEquals(garbage, TimeFormatter.formatTime(garbage))
    }
}
