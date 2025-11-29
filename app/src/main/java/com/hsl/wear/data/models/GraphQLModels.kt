package com.hsl.wear.data.models

import kotlinx.serialization.Serializable

@Serializable
data class GraphQLRequest(
    val query: String,
    val variables: Map<String, String> = emptyMap()
)

@Serializable
data class GraphQLResponse<T>(
    val data: T? = null,
    val errors: List<GraphQLError>? = null
)

@Serializable
data class GraphQLError(
    val message: String,
    val locations: List<GraphQLLocation>?
)

@Serializable
data class GraphQLLocation(
    val line: Int,
    val column: Int
)

// HSL Specific GraphQL Response Types
@Serializable
data class PlanResponse(
    val plan: Plan
)

@Serializable
data class Plan(
    val itineraries: List<ItineraryWrapper>
)

@Serializable
data class ItineraryWrapper(
    val legs: List<LegWrapper>,
    val duration: Int,
    val walkDistance: Double,
    val startTime: Long,
    val endTime: Long
)

@Serializable
data class LegWrapper(
    val mode: String,
    val route: RouteWrapper?,
    val trip: TripWrapper?,
    val from: PlaceWrapper,
    val to: PlaceWrapper,
    val distance: Double?,
    val duration: Double,
    val startTime: Long,
    val endTime: Long,
    val realTime: Boolean = false,
    val intermediateStops: List<IntermediateStopWrapper>? = null
)

@Serializable
data class RouteWrapper(
    val shortName: String? = null,
    val longName: String? = null
)

@Serializable
data class PlaceWrapper(
    val name: String,
    val lat: Double,
    val lon: Double,
    val stop: StopWrapper? = null
)

@Serializable
data class StopWrapper(
    val name: String? = null,
    val code: String? = null,
    val gtfsId: String? = null,
    val platformCode: String? = null,
    val zoneId: String? = null
)

@Serializable
data class TripWrapper(
    val gtfsId: String? = null,
    val routeShortName: String? = null,
    val tripHeadsign: String? = null
)

@Serializable
data class IntermediateStopWrapper(
    val name: String,
    val gtfsId: String? = null
)

// Autocomplete response types
@Serializable
data class AutocompleteResponse(
    val viewer: ViewerWrapper
)

@Serializable
data class ViewerWrapper(
    val stops: StopsConnection
)

@Serializable
data class StopsConnection(
    val edges: List<StopEdge>
)

@Serializable
data class StopEdge(
    val node: StopNode
)

@Serializable
data class StopNode(
    val name: String,
    val lat: Double,
    val lon: Double,
    val code: String? = null,
    val gtfsId: String? = null,
    val routes: RouteConnection
)

@Serializable
data class RouteConnection(
    val edges: List<RouteEdge>
)

@Serializable
data class RouteEdge(
    val node: RouteNode
)

@Serializable
data class RouteNode(
    val shortName: String? = null,
    val longName: String? = null
)

// Error response types
@Serializable
data class ApiError(
    val code: String,
    val message: String,
    val details: Map<String, String> = emptyMap()
)

// Real-time departures response types
@Serializable
data class RealtimeDeparturesResponse(
    val stop: StopDeparturesWrapper
)

@Serializable
data class StopDeparturesWrapper(
    val name: String,
    val stoptimesWithoutPatterns: List<StoptimeWrapper>
)

@Serializable
data class StoptimeWrapper(
    val scheduledDeparture: Int, // seconds since midnight
    val realtimeDeparture: Int,   // seconds since midnight
    val realtime: Boolean,
    val headsign: String? = null,
    val trip: TripDepartureWrapper
)

@Serializable
data class TripDepartureWrapper(
    val route: RouteDepartureWrapper
)

@Serializable
data class RouteDepartureWrapper(
    val shortName: String? = null,
    val longName: String? = null
)

// Geocoding API response types (REST API, not GraphQL)
@Serializable
data class GeocodingResponse(
    val features: List<GeocodingFeature> = emptyList()
)

@Serializable
data class GeocodingFeature(
    val geometry: GeocodingGeometry,
    val properties: GeocodingProperties
)

@Serializable
data class GeocodingGeometry(
    val coordinates: List<Double> // [lon, lat]
)

@Serializable
data class GeocodingProperties(
    val name: String? = null,
    val label: String? = null,
    val layer: String? = null,
    val localadmin: String? = null,
    val locality: String? = null,
    val neighbourhood: String? = null,
    val postalcode: String? = null,
    val street: String? = null,
    val housenumber: String? = null
)

// Trip Status response models for GetTripStatus query
@Serializable
data class TripStatusResponse(
    val trip: TripStatusWrapper
)

@Serializable
data class TripStatusWrapper(
    val gtfsId: String,
    val stoptimes: List<StopTimeWrapper>
)

@Serializable
data class StopTimeWrapper(
    val stop: StopWrapper,
    val scheduledArrival: Int,     // seconds since midnight
    val realtimeArrival: Int,       // seconds since midnight
    val arrivalDelay: Int?,          // delay in seconds
    val realtime: Boolean,
    val realtimeState: String?        // e.g., "UPDATED", "CANCELED", "SCHEDULED"
)