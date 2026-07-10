package com.hsl.wear.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class DistanceFormatterTest {

    @Test
    fun formatDistance_meters_returnsMetersString() {
        assertEquals("250 m", DistanceFormatter.formatDistance(250))
        assertEquals("999 m", DistanceFormatter.formatDistance(999))
    }

    @Test
    fun formatDistance_kilometers_returnsKmString() {
        assertEquals("1.0 km", DistanceFormatter.formatDistance(1000))
        assertEquals("1.5 km", DistanceFormatter.formatDistance(1500))
    }

    @Test
    fun formatWalkingDistance_meters_returnsCompactMeters() {
        assertEquals("250m", DistanceFormatter.formatWalkingDistance(250))
    }

    @Test
    fun formatWalkingDistance_kilometers_returnsCompactKm() {
        assertEquals("1.5km", DistanceFormatter.formatWalkingDistance(1500))
    }
}
