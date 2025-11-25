package com.hsl.wear.data.mappers

import com.hsl.wear.data.models.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Mapper for converting GraphQL API responses to domain models.
 * Consolidates mapping logic from HslRepository.
 */
object GraphQLResponseMapper {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault())

    /**
     * Formats epoch timestamp to ISO 8601 string.
     * @param timestamp Epoch milliseconds
     * @return ISO 8601 formatted timestamp
     */
    fun formatTimestamp(timestamp: Long): String {
        return dateFormat.format(Date(timestamp))
    }

    /**
     * Maps ItineraryWrapper to Itinerary domain model.
     * @param wrapper ItineraryWrapper from GraphQL response
     * @return Itinerary domain model
     */
    fun mapItineraryWrapperToItinerary(wrapper: ItineraryWrapper): Itinerary {
        val legs = wrapper.legs.map { legWrapper ->
            mapLegWrapperToLeg(legWrapper)
        }

        return Itinerary(
            id = UUID.randomUUID().toString(),
            legs = legs,
            totalDuration = wrapper.duration,
            totalWalkingDistance = wrapper.walkDistance.toInt(),
            startTimeIso = formatTimestamp(wrapper.startTime),
            endTimeIso = formatTimestamp(wrapper.endTime)
        )
    }

    /**
     * Maps LegWrapper to Leg domain model.
     * @param wrapper LegWrapper from GraphQL response
     * @return Leg domain model
     */
    fun mapLegWrapperToLeg(wrapper: LegWrapper): Leg {
        return Leg(
            mode = wrapper.mode,
            line = wrapper.trip?.routeShortName ?: wrapper.route?.shortName,
            headsign = wrapper.trip?.tripHeadsign,
            fromStopId = wrapper.from.stop?.gtfsId,
            fromStopName = wrapper.from.stop?.name ?: wrapper.from.name,
            fromPlatformCode = wrapper.from.stop?.platformCode,
            fromZoneId = wrapper.from.stop?.zoneId,
            toStopId = wrapper.to.stop?.gtfsId,
            toStopName = wrapper.to.stop?.name ?: wrapper.to.name,
            toPlatformCode = wrapper.to.stop?.platformCode,
            toZoneId = wrapper.to.stop?.zoneId,
            platform = wrapper.from.stop?.code,
            scheduledTimeIso = formatTimestamp(wrapper.startTime),
            realtimeTimeIso = if (wrapper.realTime) {
                formatTimestamp(wrapper.startTime)
            } else null,
            distance = wrapper.distance?.toInt(),
            duration = wrapper.duration.toInt(),
            lat = wrapper.from.lat,
            lon = wrapper.from.lon,
            intermediateStops = wrapper.intermediateStops?.map { it.name } ?: emptyList()
        )
    }

    /**
     * Maps list of ItineraryWrappers to list of Itineraries.
     * @param wrappers List of ItineraryWrappers from GraphQL response
     * @return List of Itinerary domain models
     */
    fun mapItinerariesToDomain(wrappers: List<ItineraryWrapper>): List<Itinerary> {
        return wrappers.map { mapItineraryWrapperToItinerary(it) }
    }
}
