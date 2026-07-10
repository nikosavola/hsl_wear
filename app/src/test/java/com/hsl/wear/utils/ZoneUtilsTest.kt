package com.hsl.wear.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test

class ZoneUtilsTest {

    @Test
    fun getRouteZones_differentZones_returnsDistinctSortedZones() {
        val zones = ZoneUtils.getRouteZones("A", "C")
        assertEquals(listOf("A", "C"), zones)
    }

    @Test
    fun crossesMultipleZones_detectsCrossing() {
        assertTrue(ZoneUtils.crossesMultipleZones("A", "B"))
        assertFalse(ZoneUtils.crossesMultipleZones("A", "A"))
        assertFalse(ZoneUtils.crossesMultipleZones("A", null))
    }

    @Test
    fun formatRouteZones_formatsCorrectly() {
        assertEquals("A", ZoneUtils.formatRouteZones("A", "A"))
        assertEquals("AC", ZoneUtils.formatRouteZones("A", "C"))
    }
    
    @Test
    fun getZoneTransitionDescription_formatsTransition() {
        assertEquals("Zone A→B", ZoneUtils.getZoneTransitionDescription("A", "B"))
        assertEquals("Zone A", ZoneUtils.getZoneTransitionDescription("A", "A"))
    }
}
