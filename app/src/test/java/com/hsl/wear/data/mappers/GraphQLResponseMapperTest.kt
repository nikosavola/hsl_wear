package com.hsl.wear.data.mappers

import com.hsl.wear.data.models.*
import com.hsl.wear.utils.constants.TransportModeConstants
import org.junit.Test
import org.junit.Assert.*
import java.time.Instant
import java.time.ZoneId

/**
 * Unit tests for GraphQLResponseMapper to ensure thread safety and correct data transformation.
 */
class GraphQLResponseMapperTest {

    @Test
    fun `formatTimestamp should format epoch milliseconds to ISO 8601 string`() {
        // Given
        val timestamp = 1704067200000L // January 1, 2024 00:00:00 UTC
        val expected = "2024-01-01T02:00:00.000+02:00" // Helsinki timezone (UTC+2 in winter)

        // When
        val result = GraphQLResponseMapper.formatTimestamp(timestamp)

        // Then - verify it's a valid ISO 8601 format
        assertTrue("Result should be a valid ISO 8601 timestamp",
            result.matches(Regex("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}[+-]\\d{2}:\\d{2}$")))

        // Verify we can parse it back (thread safety check)
        val parsed = Instant.parse(result).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        assertEquals("Parsed timestamp should match original within timezone precision",
            timestamp, parsed)
    }

    @Test
    fun `formatTimestamp should be thread safe`() {
        // Given
        val timestamps = listOf(1704067200000L, 1704067260000L, 1704067320000L)
        val results = mutableListOf<String>()
        val exceptions = mutableListOf<Exception>()

        // When - run formatting in multiple threads
        val threads = (1..10).map { threadId ->
            Thread {
                try {
                    timestamps.forEach { timestamp ->
                        val result = GraphQLResponseMapper.formatTimestamp(timestamp)
                        synchronized(results) {
                            results.add("Thread$threadId: $result")
                        }
                    }
                } catch (e: Exception) {
                    synchronized(exceptions) {
                        exceptions.add(e)
                    }
                }
            }
        }

        threads.forEach { it.start() }
        threads.forEach { it.join() }

        // Then - no exceptions should occur and all results should be valid
        assertTrue("No thread safety exceptions should occur", exceptions.isEmpty())
        assertEquals("Should have 30 results (10 threads × 3 timestamps)", 30, results.size)

        results.forEach { result ->
            val timestamp = result.substringAfter(": ")
            assertTrue("All results should be valid ISO 8601",
                timestamp.matches(Regex("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}[+-]\\d{2}:\\d{2}$")))
        }
    }

    @Test
    fun `mapLegWrapperToLeg should handle basic leg data correctly`() {
        // Given
        val legWrapper = LegWrapper(
            mode = TransportModeConstants.WALK,
            from = PlaceWrapper(
                name = "Start Point",
                lat = 60.1699,
                lon = 24.9384,
                stop = null
            ),
            to = PlaceWrapper(
                name = "End Point",
                lat = 60.1700,
                lon = 24.9385,
                stop = null
            ),
            startTime = 1704067200000L,
            endTime = 1704067260000L,
            distance = 100.0,
            duration = 600.0,
            trip = null,
            route = null,
            intermediateStops = null
        )

        // When
        val result = GraphQLResponseMapper.mapLegWrapperToLeg(legWrapper)

        // Then
        assertEquals(TransportModeConstants.WALK, result.mode)
        assertEquals("Start Point", result.fromStopName)
        assertEquals("End Point", result.toStopName)
        assertEquals(100, result.distance)
        assertEquals(600, result.duration)
        assertEquals(60.1699, result.lat ?: 0.0, 0.0001)
        assertEquals(24.9384, result.lon ?: 0.0, 0.0001)
        assertTrue("Scheduled time should be valid ISO format",
            result.scheduledTimeIso?.matches(Regex("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}\\.\\d{3}[+-]\\d{2}:\\d{2}$")) == true)
    }

    @Test
    fun `mapItineraryWrapperToItinerary should map complete itinerary`() {
        // Given
        val legWrapper = LegWrapper(
            mode = TransportModeConstants.BUS,
            from = PlaceWrapper(
                name = "Stop A",
                lat = 60.1699,
                lon = 24.9384,
                stop = StopWrapper(
                    gtfsId = "HSL:1001",
                    name = "Stop A",
                    code = "A1",
                    platformCode = "1",
                    zoneId = "A"
                )
            ),
            to = PlaceWrapper(
                name = "Stop B",
                lat = 60.1700,
                lon = 24.9385,
                stop = StopWrapper(
                    gtfsId = "HSL:1002",
                    name = "Stop B",
                    code = "B1",
                    platformCode = "2",
                    zoneId = "B"
                )
            ),
            startTime = 1704067200000L,
            endTime = 1704067260000L,
            distance = 500.0,
            duration = 600.0,
            trip = TripWrapper(
                gtfsId = "HSL:Trip:123",
                routeShortName = "75",
                tripHeadsign = "City Center"
            ),
            route = null,
            intermediateStops = null
        )

        val itineraryWrapper = ItineraryWrapper(
            legs = listOf(legWrapper),
            duration = 600,
            walkDistance = 50.0,
            startTime = 1704067200000L,
            endTime = 1704067260000L
        )

        // When
        val result = GraphQLResponseMapper.mapItineraryWrapperToItinerary(itineraryWrapper)

        // Then
        assertEquals(1, result.legs.size)
        assertEquals(600, result.totalDuration)
        assertEquals(50, result.totalWalkingDistance)
        assertEquals("HSL:1001", result.legs[0].fromStopId)
        assertEquals("Stop A", result.legs[0].fromStopName)
        assertEquals("75", result.legs[0].line)
        assertEquals("City Center", result.legs[0].headsign)
    }
}