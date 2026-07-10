package com.hsl.wear.data.repository

import com.hsl.wear.data.models.Leg
import com.hsl.wear.data.models.Location
import com.hsl.wear.data.models.LocationType
import com.hsl.wear.data.models.RouteState
import com.hsl.wear.data.store.RouteStore
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TransitRepositoryTest {

    private lateinit var hslRepository: HslRepository
    private lateinit var routeStore: RouteStore
    private lateinit var transitRepository: TransitRepository

    @Before
    fun setup() {
        hslRepository = mockk()
        routeStore = mockk(relaxed = true)
        transitRepository = TransitRepository(hslRepository, routeStore)
    }

    @Test
    fun advanceToNextLeg_success() = runTest {
        val locFrom = Location("loc1", "From", 60.0, 24.0, type = LocationType.STOP)
        val locTo = Location("loc2", "To", 60.1, 24.1, type = LocationType.STOP)
        val legs = listOf(mockk<Leg>(relaxed = true), mockk<Leg>(relaxed = true))
        val routeState = RouteState(itineraryId = "it1", legs = legs, startTimeIso = "2024-01-01T00:00:00Z", fromLocation = locFrom, toLocation = locTo, currentIndex = 0, lastUpdated = 123L)
        
        coEvery { routeStore.routeStateFlow } returns flowOf(routeState)
        transitRepository = TransitRepository(hslRepository, routeStore)

        val result = transitRepository.advanceToNextLeg()
        
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.currentIndex)
        coVerify { routeStore.saveRouteState(any()) }
    }

    @Test
    fun advanceToNextLeg_alreadyComplete_fails() = runTest {
        val locFrom = Location("loc1", "From", 60.0, 24.0, type = LocationType.STOP)
        val locTo = Location("loc2", "To", 60.1, 24.1, type = LocationType.STOP)
        val legs = listOf(mockk<Leg>(relaxed = true), mockk<Leg>(relaxed = true))
        val routeState = RouteState(itineraryId = "it1", legs = legs, startTimeIso = "2024-01-01T00:00:00Z", fromLocation = locFrom, toLocation = locTo, currentIndex = 1, lastUpdated = 123L)
        
        coEvery { routeStore.routeStateFlow } returns flowOf(routeState)
        transitRepository = TransitRepository(hslRepository, routeStore)

        val result = transitRepository.advanceToNextLeg()
        
        assertTrue(result.isFailure)
    }

    @Test
    fun moveToPreviousLeg_success() = runTest {
        val locFrom = Location("loc1", "From", 60.0, 24.0, type = LocationType.STOP)
        val locTo = Location("loc2", "To", 60.1, 24.1, type = LocationType.STOP)
        val legs = listOf(mockk<Leg>(relaxed = true), mockk<Leg>(relaxed = true))
        val routeState = RouteState(itineraryId = "it1", legs = legs, startTimeIso = "2024-01-01T00:00:00Z", fromLocation = locFrom, toLocation = locTo, currentIndex = 1, lastUpdated = 123L)
        
        coEvery { routeStore.routeStateFlow } returns flowOf(routeState)
        transitRepository = TransitRepository(hslRepository, routeStore)

        val result = transitRepository.moveToPreviousLeg()
        
        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()?.currentIndex)
        coVerify { routeStore.saveRouteState(any()) }
    }

    @Test
    fun moveToPreviousLeg_atFirstLeg_fails() = runTest {
        val locFrom = Location("loc1", "From", 60.0, 24.0, type = LocationType.STOP)
        val locTo = Location("loc2", "To", 60.1, 24.1, type = LocationType.STOP)
        val legs = listOf(mockk<Leg>(relaxed = true), mockk<Leg>(relaxed = true))
        val routeState = RouteState(itineraryId = "it1", legs = legs, startTimeIso = "2024-01-01T00:00:00Z", fromLocation = locFrom, toLocation = locTo, currentIndex = 0, lastUpdated = 123L)
        
        coEvery { routeStore.routeStateFlow } returns flowOf(routeState)
        transitRepository = TransitRepository(hslRepository, routeStore)

        val result = transitRepository.moveToPreviousLeg()
        
        assertTrue(result.isFailure)
    }

    @Test
    fun jumpToLeg_success() = runTest {
        val locFrom = Location("loc1", "From", 60.0, 24.0, type = LocationType.STOP)
        val locTo = Location("loc2", "To", 60.1, 24.1, type = LocationType.STOP)
        val legs = listOf(mockk<Leg>(relaxed = true), mockk<Leg>(relaxed = true), mockk<Leg>(relaxed = true))
        val routeState = RouteState(itineraryId = "it1", legs = legs, startTimeIso = "2024-01-01T00:00:00Z", fromLocation = locFrom, toLocation = locTo, currentIndex = 0, lastUpdated = 123L)
        
        coEvery { routeStore.routeStateFlow } returns flowOf(routeState)
        transitRepository = TransitRepository(hslRepository, routeStore)

        val result = transitRepository.jumpToLeg(2)
        
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.currentIndex)
        coVerify { routeStore.saveRouteState(any()) }
    }

    @Test
    fun jumpToLeg_invalidIndex_fails() = runTest {
        val locFrom = Location("loc1", "From", 60.0, 24.0, type = LocationType.STOP)
        val locTo = Location("loc2", "To", 60.1, 24.1, type = LocationType.STOP)
        val legs = listOf(mockk<Leg>(relaxed = true), mockk<Leg>(relaxed = true))
        val routeState = RouteState(itineraryId = "it1", legs = legs, startTimeIso = "2024-01-01T00:00:00Z", fromLocation = locFrom, toLocation = locTo, currentIndex = 0, lastUpdated = 123L)
        
        coEvery { routeStore.routeStateFlow } returns flowOf(routeState)
        transitRepository = TransitRepository(hslRepository, routeStore)

        val result = transitRepository.jumpToLeg(2)
        
        assertTrue(result.isFailure)
    }
}
