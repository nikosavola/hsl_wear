package com.hsl.wear.network

import com.hsl.wear.utils.constants.NetworkConstants
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object GraphQLQueries {

    fun getCurrentLocalTime(): Pair<String, String> {
        val now = ZonedDateTime.now(ZoneId.of("Europe/Helsinki"))
            .minusMinutes(NetworkConstants.TIME_ADJUSTMENT_MINUTES.toLong())
        
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss", Locale.US)
        
        return Pair(now.format(dateFormatter), now.format(timeFormatter))
    }

    fun planRoute(
        fromLat: Double,
        fromLon: Double,
        toLat: Double,
        toLon: Double,
        numItineraries: Int = NetworkConstants.DEFAULT_NUM_ITINERARIES
    ): String {
        return """
            query Plan(${"$"}date: String!, ${"$"}time: String!) {
              plan(
                from: {lat: $fromLat, lon: $fromLon}
                to: {lat: $toLat, lon: $toLon}
                date: ${"$"}date
                time: ${"$"}time
                numItineraries: $numItineraries
              ) {
                itineraries {
                  duration
                  startTime
                  endTime
                  walkDistance
                  legs {
                    mode
                    startTime
                    endTime
                    duration
                    distance
                    realTime
                    from {
                      name
                      lat
                      lon
                      stop {
                        name
                        code
                        gtfsId
                        platformCode
                        zoneId
                      }
                    }
                    to {
                      name
                      lat
                      lon
                      stop {
                        name
                        code
                        gtfsId
                        platformCode
                        zoneId
                      }
                    }
                    route {
                      shortName
                      longName
                    }
                    trip {
                      gtfsId
                      routeShortName
                      tripHeadsign
                    }
                    intermediateStops {
                      name
                      gtfsId
                    }
                  }
                }
              }
            }
        """.trimIndent()
    }

    fun getTripStatus(): String {
        return """
            query GetTripStatus(${"$"}tripId: String!) {
              trip(id: ${"$"}tripId) {
                gtfsId
                stoptimes {
                  stop {
                    name
                    gtfsId
                  }
                  scheduledArrival
                  realtimeArrival
                  arrivalDelay
                  realtime
                  realtimeState
                }
              }
            }
        """.trimIndent()
    }
}
