package com.hsl.wear.data.repository

import com.hsl.wear.data.models.*
import com.hsl.wear.data.mappers.GeocodingMapper
import com.hsl.wear.data.mappers.GraphQLResponseMapper
import com.hsl.wear.network.GeocodingClient
import com.hsl.wear.network.GraphQLClient
import com.hsl.wear.network.GraphQLQueries
import com.hsl.wear.utils.constants.LocationConstants
import com.hsl.wear.utils.constants.NetworkConstants
import com.hsl.wear.utils.constants.TimeConstants
import com.hsl.wear.utils.constants.TransportModeConstants
import com.hsl.wear.utils.ErrorMessages
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.atStartOfDayIn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HslRepository @Inject constructor(
    private val graphQLClient: GraphQLClient,
    private val geocodingClient: GeocodingClient
) {

    suspend fun geocodeSearch(searchText: String): Result<List<AutocompleteResult>> {
        if (searchText.length < LocationConstants.MIN_SEARCH_TEXT_LENGTH) {
            return Result.success(emptyList())
        }

        return ErrorMessages.safeExecute(
            tag = "HslRepository",
            operation = "geocodeSearch for '$searchText'"
        ) {
            val result = geocodingClient.searchLocations(searchText)
            
            result.map { response ->
                val results = GeocodingMapper.mapFeaturesToAutocompleteResults(
                    features = response.features,
                    idPrefix = "search",
                    defaultName = LocationConstants.UNKNOWN_LOCATION_FALLBACK
                )
                results
            }
        }
    }

    suspend fun reverseGeocodeResults(lat: Double, lon: Double): Result<List<AutocompleteResult>> {
        return ErrorMessages.safeExecute(
            tag = "HslRepository",
            operation = "reverseGeocodeResults for ($lat, $lon)"
        ) {
            val result = geocodingClient.reverseGeocode(lat, lon)

            result.map { response ->
                val results = GeocodingMapper.mapFeaturesToAutocompleteResults(
                    features = response.features,
                    idPrefix = "current_location",
                    defaultName = "Nearby Location"
                )
                results
            }
        }
    }

    suspend fun planRoute(
        fromLocation: Location,
        toLocation: Location
    ): Result<List<Itinerary>> {
        return ErrorMessages.safeExecute(
            tag = "HslRepository",
            operation = "planRoute from (${fromLocation.lat}, ${fromLocation.lon}) to (${toLocation.lat}, ${toLocation.lon})"
        ) {
            val query = GraphQLQueries.planRoute(
                fromLat = fromLocation.lat,
                fromLon = fromLocation.lon,
                toLat = toLocation.lat,
                toLon = toLocation.lon
            )

            val (date, time) = GraphQLQueries.getCurrentLocalTime()

            val result = graphQLClient.executeQuery<com.hsl.wear.data.models.PlanResponse>(
                endpoint = NetworkConstants.HSL_ENDPOINT,
                query = query,
                variables = mapOf("date" to date, "time" to time)
            )

            result.map { response ->
                GraphQLResponseMapper.mapItinerariesToDomain(response.plan.itineraries)
            }
        }
    }

    private fun getCurrentTimeString(): String {
        val now = Clock.System.now()
        val nowInUtc = now.toLocalDateTime(TimeZone.UTC)
        return "${nowInUtc.year}-${nowInUtc.monthNumber.toString().padStart(2, '0')}-${nowInUtc.dayOfMonth.toString().padStart(2, '0')}T${nowInUtc.hour.toString().padStart(2, '0')}:${nowInUtc.minute.toString().padStart(2, '0')}:00.000Z"
    }

    /**
     * Gets real-time status for a specific trip using trip.gtfsId.
     * @param tripGtfsId The GTFS trip ID to get status for
     * @return Result containing TripStatusResponse or failure
     */
    suspend fun getTripStatus(tripGtfsId: String): Result<TripStatusResponse> {
        return ErrorMessages.safeExecute(
            tag = "HslRepository",
            operation = "getTripStatus for '$tripGtfsId'"
        ) {
            val query = GraphQLQueries.getTripStatus()
            val variables = mapOf("tripId" to tripGtfsId)

            val result = graphQLClient.executeQuery<TripStatusResponse>(
                endpoint = NetworkConstants.HSL_ENDPOINT,
                query = query,
                variables = variables
            )

            result.onSuccess { response ->
                response
            }.onFailure { error ->
                Result.failure<TripStatusResponse>(error)
            }
        }
    }

    /**
     * Gets real-time status for multiple trips (batch operation).
     * @param tripGtfsIds List of GTFS trip IDs to get status for
     * @return Result containing map of trip ID to TripStatusResponse
     */
    suspend fun getMultipleTripStatuses(tripGtfsIds: List<String>): Result<Map<String, TripStatusResponse>> {
        return ErrorMessages.safeExecute(
            tag = "HslRepository",
            operation = "getMultipleTripStatuses for ${tripGtfsIds.size} trips"
        ) {
            val results = mutableMapOf<String, TripStatusResponse>()
            val errors = mutableListOf<Exception>()

            // Execute requests sequentially to avoid overwhelming the API
            tripGtfsIds.forEach { tripId ->
                getTripStatus(tripId)
                    .onSuccess { tripStatus ->
                        results[tripId] = tripStatus
                    }
                    .onFailure { error ->
                        errors.add(Exception(error.message ?: "Unknown error"))
                    }
            }

            if (results.isNotEmpty()) {
                Result.success(results)
            } else {
                Result.failure(Exception("All trip status requests failed: ${errors.joinToString()}"))
            }
        }
    }

    /**
     * Updates legs in a route state with real-time data.
     * @param routeState Current route state to update
     * @return Result containing updated route state or failure
     */
    suspend fun updateRouteStateWithRealTimeData(routeState: RouteState): Result<RouteState> {
        return ErrorMessages.safeExecute(
            tag = "HslRepository",
            operation = "updateRouteStateWithRealTimeData for route with ${routeState.legs.size} legs"
        ) {
            val tripIdsToUpdate = routeState.legs
                .filter { !it.isWalking && it.tripGtfsId != null }
                .mapNotNull { it.tripGtfsId }
                .distinct()

            if (tripIdsToUpdate.isEmpty()) {
                return@safeExecute Result.success(routeState)
            }

            val tripStatuses = getMultipleTripStatuses(tripIdsToUpdate)

            tripStatuses.map { statuses ->
                val updatedLegs = routeState.legs.map { leg ->
                    if (leg.tripGtfsId != null && statuses.containsKey(leg.tripGtfsId)) {
                        val tripStatus = statuses[leg.tripGtfsId]!!
                        GraphQLResponseMapper.updateLegWithRealTimeData(
                            leg = leg,
                            tripStatus = tripStatus,
                            targetStopId = leg.toStopId
                        )
                    } else {
                        leg
                    }
                }

                routeState.copy(
                    legs = updatedLegs,
                    lastUpdated = System.currentTimeMillis()
                )
            }
        }
    }
}