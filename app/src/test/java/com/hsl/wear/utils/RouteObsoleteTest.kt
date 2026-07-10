package com.hsl.wear.utils

import com.hsl.wear.data.models.Location
import com.hsl.wear.data.models.RouteState
import com.hsl.wear.data.models.Leg
import org.junit.Assert.assertFalse
import org.junit.Test
import java.lang.reflect.Method

class RouteObsoleteTest {

    @Test
    fun testIsRouteObsolete_withGarbageTimestamp_returnsFalse() {
        val dummyLocation = Location("id", "name", 0.0, 0.0)
        
        // A leg with a malformed scheduledTimeIso and null realtimeTimeIso
        val malformedLeg = Leg(
            startTime = 0L,
            endTime = 0L,
            mode = "WALK",
            duration = 600,
            distance = 500.0,
            isWalking = true,
            fromStopName = "Start",
            toStopName = "End",
            scheduledTimeIso = "invalid-time",
            realtimeTimeIso = null,
            line = null,
            intermediateStops = emptyList(),
            fromLat = 0.0,
            fromLon = 0.0,
            toLat = 0.0,
            toLon = 0.0
        )
        
        val routeState = RouteState(
            legs = listOf(malformedLeg),
            startTimeIso = "invalid-time",
            fromLocation = dummyLocation,
            toLocation = dummyLocation,
            currentIndex = 0,
            isComplete = false
        )
        
        // Test RouteTrackingViewModel.isRouteObsolete
        val vmClass = Class.forName("com.hsl.wear.ui.viewmodel.RouteTrackingViewModel")
        val isRouteObsoleteVm: Method = vmClass.getDeclaredMethod("isRouteObsolete", RouteState::class.java, Long::class.java)
        isRouteObsoleteVm.isAccessible = true
        
        // Without an instance of ViewModel, wait... isRouteObsolete is not static!
        // We can create a mock ViewModel if needed, but easier is to test CurrentLegTileService
        // Wait, RouteTrackingViewModel requires TransitRepository to instantiate.
        // Let's test CurrentLegTileService instead.
        
        val tileClass = Class.forName("com.hsl.wear.tiles.CurrentLegTileService")
        val isRouteObsoleteTile: Method = tileClass.getDeclaredMethod("isRouteObsolete", RouteState::class.java)
        isRouteObsoleteTile.isAccessible = true
        
        // We can just instantiate it since it's an Android Service with empty constructor
        val tileInstance = tileClass.getDeclaredConstructor().newInstance()
        
        val resultTile = isRouteObsoleteTile.invoke(tileInstance, routeState) as Boolean
        
        // Since the timestamp is garbage, parseIsoTime returns null.
        // The method should immediately return false.
        assertFalse("Route should not be considered obsolete with garbage timestamp", resultTile)
    }
}
