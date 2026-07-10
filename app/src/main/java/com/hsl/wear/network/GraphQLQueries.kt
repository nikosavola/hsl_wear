package com.hsl.wear.network

import com.hsl.wear.utils.constants.NetworkConstants
import java.text.SimpleDateFormat
import java.util.*

object GraphQLQueries {

    private fun getCurrentLocalTime(): Pair<String, String> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.US)
        // Subtract 5 minutes to catch routes that are about to depart
        val now = Date(System.currentTimeMillis() - (NetworkConstants.TIME_ADJUSTMENT_MINUTES * 60 * 1000))
        return Pair(dateFormat.format(now), timeFormat.format(now))
    }

    fun planRoute(
        fromLat: Double,
        fromLon: Double,
        toLat: Double,
        toLon: Double,
        numItineraries: Int = NetworkConstants.DEFAULT_NUM_ITINERARIES
    ): String {
        val (date, time) = getCurrentLocalTime()
        return """
            query Plan {
              plan(
                from: {lat: $fromLat, lon: $fromLon}
                to: {lat: $toLat, lon: $toLon}
                date: "$date"
                time: "$time"
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
            query GetTripStatus(""" + "$" + """tripId: String!) {
              trip(id: """ + "$" + """tripId) {
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