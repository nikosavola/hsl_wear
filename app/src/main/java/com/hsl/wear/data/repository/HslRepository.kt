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
        android.util.Log.d("HslRepository", "geocodeSearch called with: $searchText")
        if (searchText.length < LocationConstants.MIN_SEARCH_TEXT_LENGTH) {
            android.util.Log.d("HslRepository", "Search text too short, returning empty list")
            return Result.success(emptyList())
        }

        return ErrorMessages.safeExecute(
            tag = "HslRepository",
            operation = "geocodeSearch for '$searchText'"
        ) {
            val result = geocodingClient.searchLocations(searchText)
            
            result.map { response ->
                android.util.Log.d("HslRepository", "Got ${response.features.size} features from geocoding API")
                val results = GeocodingMapper.mapFeaturesToAutocompleteResults(
                    features = response.features,
                    idPrefix = "search",
                    defaultName = LocationConstants.UNKNOWN_LOCATION_FALLBACK
                )
                android.util.Log.d("HslRepository", "Returning ${results.size} autocomplete results")
                results
            }
        }
    }

    suspend fun reverseGeocodeResults(lat: Double, lon: Double): Result<List<AutocompleteResult>> {
        android.util.Log.d("HslRepository", "reverseGeocodeResults called with: $lat, $lon")

        return withContext(Dispatchers.IO) {
            try {
                val result = geocodingClient.reverseGeocode(lat, lon)

                result.map { response ->
                    android.util.Log.d("HslRepository", "Got ${response.features.size} features from reverse geocoding API")
                    val results = GeocodingMapper.mapFeaturesToAutocompleteResults(
                        features = response.features,
                        idPrefix = "current_location",
                        defaultName = "Nearby Location"
                    )
                    android.util.Log.d("HslRepository", "Returning ${results.size} reverse geocoding results")
                    results
                }
            } catch (e: Exception) {
                android.util.Log.e("HslRepository", "reverseGeocodeResults failed", e)
                Result.failure(e)
            }
        }
    }

    suspend fun autocompleteStops(searchText: String): Result<List<AutocompleteResult>> {
        if (searchText.length < LocationConstants.MIN_SEARCH_TEXT_LENGTH) {
            return Result.success(emptyList())
        }

        return ErrorMessages.safeExecute(
            tag = "HslRepository",
            operation = "autocompleteStops for '$searchText'"
        ) {
            val query = GraphQLQueries.autocompleteStops(searchText)
            val result = graphQLClient.executeQuery<com.hsl.wear.data.models.AutocompleteResponse>(
                endpoint = NetworkConstants.HSL_ENDPOINT_V1,
                query = query
            )

            result.map { response ->
                response.viewer.stops.edges.mapNotNull { edge: com.hsl.wear.data.models.StopEdge ->
                    val node = edge.node
                    val lines = node.routes.edges.mapNotNull { routeEdge ->
                        routeEdge.node.shortName
                    }.distinct()

                    AutocompleteResult(
                        id = node.gtfsId ?: node.code ?: "${node.lat},${node.lon}",
                        name = node.name,
                        lat = node.lat,
                        lon = node.lon,
                        type = LocationType.STOP,
                        stopCode = node.code,
                        lines = lines
                    )
                }
            }
        }
    }

    suspend fun planRoute(
        fromLocation: Location,
        toLocation: Location
    ): Result<List<Itinerary>> {
        android.util.Log.d("HslRepository", "planRoute called from (${fromLocation.lat}, ${fromLocation.lon}) to (${toLocation.lat}, ${toLocation.lon})")
        return withContext(Dispatchers.IO) {
            try {
                val query = GraphQLQueries.planRoute(
                    fromLat = fromLocation.lat,
                    fromLon = fromLocation.lon,
                    toLat = toLocation.lat,
                    toLon = toLocation.lon
                )
                android.util.Log.d("HslRepository", "GraphQL Query: ${query.take(200)}")

                val result = graphQLClient.executeQuery<com.hsl.wear.data.models.PlanResponse>(
                    endpoint = NetworkConstants.HSL_ENDPOINT,
                    query = query
                )

                result.map { response ->
                    android.util.Log.d("HslRepository", "Got ${response.plan.itineraries.size} itineraries")
                    GraphQLResponseMapper.mapItinerariesToDomain(response.plan.itineraries)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getRealtimeDepartures(stopId: String): Result<List<Leg>> {
        return withContext(Dispatchers.IO) {
            try {
                val query = GraphQLQueries.getRealtimeDepartures(stopId)
                val result = graphQLClient.executeQuery<com.hsl.wear.data.models.RealtimeDeparturesResponse>(
                    endpoint = NetworkConstants.HSL_ENDPOINT_V1,
                    query = query
                )

                result.map { response ->
                    response.stop.stoptimesWithoutPatterns.map { stoptime ->
                        // Convert seconds since midnight to milliseconds timestamp
                        val today = Clock.System.now()
                        val todayInUtc = today.toLocalDateTime(TimeZone.UTC)
                        val todayMidnightMillis = todayInUtc.date.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()

                        val scheduledMillis = todayMidnightMillis + (stoptime.scheduledDeparture * TimeConstants.MILLISECONDS_IN_SECOND)
                        val realtimeMillis = todayMidnightMillis + (stoptime.realtimeDeparture * TimeConstants.MILLISECONDS_IN_SECOND)

                        Leg(
                            mode = TransportModeConstants.BUS, // Could be inferred from route type
                            line = stoptime.trip.route.shortName,
                            headsign = stoptime.headsign,
                            fromStopId = stopId,
                            fromStopName = response.stop.name,
                            fromPlatformCode = null,
                            fromZoneId = null,
                            toStopId = null,
                            toStopName = stoptime.headsign ?: LocationConstants.UNKNOWN_LOCATION_FALLBACK,
                            toPlatformCode = null,
                            toZoneId = null,
                            platform = null,
                            scheduledTimeIso = GraphQLResponseMapper.formatTimestamp(scheduledMillis),
                            realtimeTimeIso = if (stoptime.realtime) GraphQLResponseMapper.formatTimestamp(realtimeMillis) else null,
                            distance = null,
                            duration = 0,
                            lat = null,
                            lon = null,
                            intermediateStops = emptyList()
                        )
                    }
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun getCurrentTimeString(): String {
        val now = Clock.System.now()
        val nowInUtc = now.toLocalDateTime(TimeZone.UTC)
        return "${nowInUtc.year}-${nowInUtc.monthNumber.toString().padStart(2, '0')}-${nowInUtc.dayOfMonth.toString().padStart(2, '0')}T${nowInUtc.hour.toString().padStart(2, '0')}:${nowInUtc.minute.toString().padStart(2, '0')}:00.000Z"
    }
}