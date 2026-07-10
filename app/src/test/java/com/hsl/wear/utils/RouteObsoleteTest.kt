package com.hsl.wear.utils

import com.hsl.wear.data.models.Leg
import com.hsl.wear.data.models.RouteState
import org.junit.Assert.assertFalse
import org.junit.Test

/**
 * Regression test for issue 003: a malformed timestamp must never cause an active
 * route to be auto-cleared. `isRouteObsolete` parses the final leg's time and, when
 * parsing fails, returns false (route kept) instead of treating the failure as "now"
 * and clearing the journey mid-trip.
 *
 * `CurrentLegTileService.isRouteObsolete` is pure logic (parseIsoTime + arithmetic,
 * no Android framework calls), so it is exercised here by reflection.
 */
class RouteObsoleteTest {

    private fun malformedRouteState(): RouteState {
        val malformedLeg = Leg(
            mode = "WALK",
            line = null,
            headsign = null,
            fromStopId = null,
            fromStopName = "Start",
            fromPlatformCode = null,
            fromZoneId = null,
            toStopId = null,
            toStopName = "End",
            toPlatformCode = null,
            toZoneId = null,
            platform = null,
            scheduledTimeIso = "invalid-time",
            realtimeTimeIso = null,
            distance = 500,
            duration = 600,
            lat = null,
            lon = null
        )
        return RouteState(
            itineraryId = "test-itinerary",
            legs = listOf(malformedLeg),
            startTimeIso = "invalid-time"
        )
    }

    @Test
    fun tileServiceIsRouteObsolete_withGarbageTimestamp_keepsRoute() {
        val tileClass = Class.forName("com.hsl.wear.tiles.CurrentLegTileService")
        val method = tileClass.getDeclaredMethod("isRouteObsolete", RouteState::class.java)
            .apply { isAccessible = true }
        val instance = tileClass.getDeclaredConstructor().newInstance()

        val result = method.invoke(instance, malformedRouteState()) as Boolean

        assertFalse("A malformed timestamp must not mark an active route obsolete", result)
    }
}
