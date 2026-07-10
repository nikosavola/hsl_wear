package com.hsl.wear.data.mappers

import com.hsl.wear.data.models.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Mapper for converting GraphQL API responses to domain models.
 * Consolidates mapping logic from HslRepository.
 */
object GraphQLResponseMapper {

    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
        .withLocale(Locale.US)

    // HSL/Digitransit transit times are anchored to the feed timezone, so format
    // timestamps in Europe/Helsinki rather than the device's zone. This keeps the
    // emitted offset (+02:00/+03:00) correct regardless of where the watch is.
    private val helsinkiZone = ZoneId.of("Europe/Helsinki")

    /**
     * Formats epoch timestamp to ISO 8601 string.
     * @param timestamp Epoch milliseconds
     * @return ISO 8601 formatted timestamp
     */
    fun formatTimestamp(timestamp: Long): String {
        return Instant.ofEpochMilli(timestamp)
            .atZone(helsinkiZone)
            .format(dateTimeFormatter)
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
            intermediateStops = wrapper.intermediateStops?.map { it.name } ?: emptyList(),
            tripGtfsId = wrapper.trip?.gtfsId // Capture trip ID for real-time updates
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

    /**
     * Updates a Leg with real-time delay information from trip status.
     * @param leg The original leg to update
     * @param tripStatus Trip status response containing delay information
     * @param targetStopId The stop ID to get delay info for (typically the leg's destination)
     * @return Updated Leg with delay information
     */
    fun updateLegWithRealTimeData(
        leg: Leg,
        tripStatus: TripStatusResponse,
        targetStopId: String?
    ): Leg {
        val stopTime = tripStatus.trip.stoptimes
            .find { stopTime -> stopTime.stop.gtfsId == targetStopId }

        return if (stopTime != null && stopTime.realtime && stopTime.arrivalDelay != null) {
            // Parse original scheduled time from ISO format
            val originalTime = leg.scheduledTimeIso?.let {
                try {
                    Instant.parse(it)
                        .atZone(ZoneId.systemDefault())
                        .toInstant()
                        .toEpochMilli()
                } catch (e: Exception) {
                    System.currentTimeMillis()
                }
            } ?: System.currentTimeMillis()

            // Apply delay (arrivalDelay is in seconds, convert to milliseconds)
            val updatedTime = originalTime + (stopTime.arrivalDelay * 1000L)

            leg.copy(
                realTimeDelay = stopTime.arrivalDelay,
                lastRealTimeUpdate = System.currentTimeMillis(),
                realtimeTimeIso = formatTimestamp(updatedTime)
            )
        } else {

            val updatedLeg = leg.copy(
                lastRealTimeUpdate = System.currentTimeMillis()
            )

            updatedLeg
        }
    }

    /**
     * Extracts delay information from trip status response.
     * @param tripStatus Trip status response
     * @param stopId Target stop ID
     * @return Pair of delay in seconds and whether trip has real-time data
     */
    fun extractDelayInfo(tripStatus: TripStatusResponse, stopId: String?): Pair<Int?, Boolean> {
        val stopTime = tripStatus.trip.stoptimes
            .find { stopTime -> stopTime.stop.gtfsId == stopId }

        return Pair(
            stopTime?.arrivalDelay,
            stopTime?.realtime ?: false
        )
    }
}
