package com.hsl.wear.tiles

import android.content.Context
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.ColorBuilders.argb
import androidx.wear.protolayout.DeviceParametersBuilders.DeviceParameters
import androidx.wear.protolayout.DimensionBuilders.dp
import androidx.wear.protolayout.DimensionBuilders.sp
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.LayoutElementBuilders.Box
import androidx.wear.protolayout.LayoutElementBuilders.Column
import androidx.wear.protolayout.LayoutElementBuilders.FontStyle
import androidx.wear.protolayout.LayoutElementBuilders.LayoutElement
import androidx.wear.protolayout.LayoutElementBuilders.Spacer
import androidx.wear.protolayout.LayoutElementBuilders.Text
import androidx.wear.protolayout.ModifiersBuilders.Clickable
import androidx.wear.protolayout.ModifiersBuilders.Modifiers
import androidx.wear.protolayout.ResourceBuilders.Resources
import androidx.wear.protolayout.TimelineBuilders.Timeline
import androidx.wear.protolayout.TimelineBuilders.TimelineEntry
import androidx.wear.protolayout.TypeBuilders
import androidx.wear.protolayout.expression.DynamicBuilders
import androidx.wear.protolayout.expression.DynamicBuilders.DynamicDuration
import androidx.wear.protolayout.expression.DynamicBuilders.DynamicInstant
import androidx.wear.protolayout.expression.DynamicBuilders.DynamicString
import androidx.wear.tiles.RequestBuilders.ResourcesRequest
import androidx.wear.tiles.RequestBuilders.TileRequest
import androidx.wear.tiles.TileBuilders.Tile
import androidx.wear.tiles.TileService
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.hsl.wear.data.models.Leg
import com.hsl.wear.data.models.RouteState
import java.time.Instant
import com.hsl.wear.data.store.RouteStore
import com.hsl.wear.utils.TimeFormatter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class CurrentLegTileService : TileService() {

    @Inject
    lateinit var routeStore: RouteStore

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onTileRequest(requestParams: TileRequest): ListenableFuture<Tile> {
        return Futures.immediateFuture(
            runBlocking {
                var routeState = routeStore.routeStateFlow.first()

                // Check if route is obsolete and clear it
                if (routeState != null && isRouteObsolete(routeState)) {
                    android.util.Log.d("CurrentLegTileService", "Route is obsolete, clearing it")
                    routeStore.clearRouteState()
                    routeState = null
                }

                android.util.Log.d("CurrentLegTileService", "Tile request - RouteState: ${routeState != null}, CurrentIndex: ${routeState?.currentIndex}")

                Tile.Builder()
                    .setResourcesVersion(RESOURCES_VERSION)
                    .setTileTimeline(buildTimeline(routeState))
                    .setFreshnessIntervalMillis(300_000) // 5 minutes - dynamic expressions + manual refresh handle updates
                    .build()
            }
        )
    }

    private fun buildTimeline(routeState: RouteState?): Timeline {
        val timelineBuilder = Timeline.Builder()
        val currentTime = System.currentTimeMillis()

        if (routeState == null || routeState.legs.isEmpty()) {
            // No active route - show empty state
            timelineBuilder.addTimelineEntry(
                TimelineEntry.Builder()
                    .setLayout(
                        LayoutElementBuilders.Layout.Builder()
                            .setRoot(tileLayout(context = this@CurrentLegTileService, transitLeg = null))
                            .build()
                    )
                    .build()
            )
            return timelineBuilder.build()
        }

        // Get all transit legs with their indices
        val transitLegs = routeState.legs
            .mapIndexed { index, leg -> index to leg }
            .filter { !it.second.isWalking }

        if (transitLegs.isEmpty()) {
            timelineBuilder.addTimelineEntry(
                TimelineEntry.Builder()
                    .setLayout(
                        LayoutElementBuilders.Layout.Builder()
                            .setRoot(tileLayout(context = this@CurrentLegTileService, transitLeg = null))
                            .build()
                    )
                    .build()
            )
            return timelineBuilder.build()
        }

        // Create timeline entries for automatic leg switching
        var previousArrivalTime: Long? = null

        transitLegs.forEachIndexed { transitIndex, (legIndex, leg) ->
            val departureTime = TimeFormatter.parseIsoTime(leg.realtimeTimeIso ?: leg.scheduledTimeIso)
            val arrivalTime = departureTime + (leg.duration * 1000)

            // Calculate validity period
            val validityStart = if (transitIndex == 0) {
                // First leg: start from route start OR current time (whichever is later)
                val routeStartTime = TimeFormatter.parseIsoTime(routeState.startTimeIso)
                maxOf(routeStartTime, currentTime - 60000) // Allow 1 min in past
            } else {
                // Subsequent legs: from when previous leg ended
                previousArrivalTime ?: departureTime
            }

            val validityEnd = if (transitIndex == transitLegs.size - 1) {
                // Last leg: extend validity far into future
                arrivalTime + (24 * 60 * 60 * 1000) // +24 hours
            } else {
                arrivalTime
            }

            // Only add entry if validity period is valid (start < end and end >= now)
            if (validityStart < validityEnd && validityEnd >= currentTime) {
                timelineBuilder.addTimelineEntry(
                    TimelineEntry.Builder()
                        .setLayout(
                            LayoutElementBuilders.Layout.Builder()
                                .setRoot(
                                    tileLayout(
                                        context = this@CurrentLegTileService,
                                        transitLeg = leg,
                                        legIndex = legIndex,
                                        routeState = routeState
                                    )
                                )
                                .build()
                        )
                        .setValidity(
                            androidx.wear.protolayout.TimelineBuilders.TimeInterval.Builder()
                                .setStartMillis(validityStart)
                                .setEndMillis(validityEnd)
                                .build()
                        )
                        .build()
                )
            }

            previousArrivalTime = arrivalTime
        }

        // If no valid entries were added (all in past), add a fallback
        if (timelineBuilder.build().timelineEntries.isEmpty()) {
            val lastLeg = transitLegs.lastOrNull()
            timelineBuilder.addTimelineEntry(
                TimelineEntry.Builder()
                    .setLayout(
                        LayoutElementBuilders.Layout.Builder()
                            .setRoot(
                                tileLayout(
                                    context = this@CurrentLegTileService,
                                    transitLeg = lastLeg?.second,
                                    legIndex = lastLeg?.first ?: -1
                                )
                            )
                            .build()
                    )
                    .build()
            )
        }

        return timelineBuilder.build()
    }

    override fun onTileResourcesRequest(requestParams: ResourcesRequest): ListenableFuture<Resources> {
        return Futures.immediateFuture(
            Resources.Builder()
                .setVersion(RESOURCES_VERSION)
                .build()
        )
    }

    private fun tileLayout(
        context: Context,
        transitLeg: Leg?,
        legIndex: Int = -1,
        routeState: RouteState? = null
    ): LayoutElement {
        // Determine which leg index to navigate to when clicked
        val targetLegIndex = if (transitLeg != null && routeState != null) {
            // Check if current leg has arrived
            val currentTime = System.currentTimeMillis()
            val departureTime = TimeFormatter.parseIsoTime(transitLeg.realtimeTimeIso ?: transitLeg.scheduledTimeIso)
            val arrivalTime = departureTime + (transitLeg.duration * 1000)
            val hasArrived = currentTime >= arrivalTime

            // If arrived and there's a next leg, navigate to next leg
            if (hasArrived && legIndex < routeState.legs.size - 1) {
                legIndex + 1
            } else {
                legIndex
            }
        } else {
            legIndex
        }

        // Tile with content and refresh button at bottom
        // Use expand to fill available space instead of fixed size
        return Box.Builder()
            .setWidth(androidx.wear.protolayout.DimensionBuilders.expand())
            .setHeight(androidx.wear.protolayout.DimensionBuilders.expand())
            .setModifiers(
                Modifiers.Builder()
                    .setPadding(
                        androidx.wear.protolayout.ModifiersBuilders.Padding.Builder()
                            .setAll(dp(8f))
                            .build()
                    )
                    .build()
            )
            .addContent(
                Column.Builder()
                    .setWidth(androidx.wear.protolayout.DimensionBuilders.expand())
                    .setHeight(androidx.wear.protolayout.DimensionBuilders.expand())
                    .addContent(
                        Box.Builder()
                            .setWidth(androidx.wear.protolayout.DimensionBuilders.expand())
                            .setHeight(androidx.wear.protolayout.DimensionBuilders.expand())
                            .setModifiers(
                                Modifiers.Builder()
                                    .setPadding(
                                        androidx.wear.protolayout.ModifiersBuilders.Padding.Builder()
                                            .setBottom(dp(4f))
                                            .build()
                                    )
                                    .build()
                            )
                            .setModifiers(
                                Modifiers.Builder()
                                    .setClickable(
                                        Clickable.Builder()
                                            .setId("open_app")
                                            .setOnClick(
                                                ActionBuilders.LaunchAction.Builder()
                                                    .setAndroidActivity(
                                                        ActionBuilders.AndroidActivity.Builder()
                                                            .setPackageName(context.packageName)
                                                            .setClassName("com.hsl.wear.MainActivity")
                                                            .apply {
                                                                if (transitLeg != null) {
                                                                    addKeyToExtraMapping(
                                                                        "destination",
                                                                        ActionBuilders.AndroidStringExtra.Builder()
                                                                            .setValue("route_tracking")
                                                                            .build()
                                                                    )
                                                                    addKeyToExtraMapping(
                                                                        "leg_index",
                                                                        ActionBuilders.AndroidIntExtra.Builder()
                                                                            .setValue(targetLegIndex)
                                                                            .build()
                                                                    )
                                                                }
                                                            }
                                                            .build()
                                                    )
                                                    .build()
                                            )
                                            .build()
                                    )
                                    .build()
                            )
                            .addContent(
                                if (transitLeg != null) {
                                    transitLegContent(transitLeg)
                                } else {
                                    noActiveLegContent()
                                }
                            )
                            .build()
                    )
                    .addContent(
                        Spacer.Builder()
                            .setHeight(dp(2f))
                            .build()
                    )
                    .addContent(
                        createRefreshButton(context)
                    )
                    .build()
            )
            .build()
    }

    private fun createRefreshButton(context: Context): LayoutElement {
        return Box.Builder()
            .setWidth(androidx.wear.protolayout.DimensionBuilders.expand())
            .setHeight(androidx.wear.protolayout.DimensionBuilders.wrap())
            .setModifiers(
                Modifiers.Builder()
                    .setClickable(
                        Clickable.Builder()
                            .setId("refresh_tile")
                            .setOnClick(
                                // LoadAction forces immediate onTileRequest() call
                                ActionBuilders.LoadAction.Builder()
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
            .addContent(
                Text.Builder()
                    .setText("↻")
                    .setFontStyle(
                        FontStyle.Builder()
                            .setSize(sp(20f))
                            .setColor(argb(0xFF888888.toInt()))
                            .build()
                    )
                    .build()
            )
            .build()
    }

    private fun createNavigationButtons(context: Context): LayoutElement {
        return androidx.wear.protolayout.LayoutElementBuilders.Row.Builder()
            .setWidth(dp(TILE_SIZE))
            .setHeight(dp(30f))
            .addContent(
                // Previous button
                Box.Builder()
                    .setWidth(dp(60f))
                    .setHeight(dp(30f))
                    .setModifiers(
                        Modifiers.Builder()
                            .setClickable(
                                Clickable.Builder()
                                    .setId("prev_leg")
                                    .setOnClick(
                                        ActionBuilders.LaunchAction.Builder()
                                            .setAndroidActivity(
                                                ActionBuilders.AndroidActivity.Builder()
                                                    .setPackageName(context.packageName)
                                                    .setClassName("com.hsl.wear.tiles.TileActionReceiver")
                                                    .addKeyToExtraMapping(
                                                        TileActionReceiver.EXTRA_ACTION,
                                                        ActionBuilders.AndroidStringExtra.Builder()
                                                            .setValue(TileActionReceiver.ACTION_PREV_LEG)
                                                            .build()
                                                    )
                                                    .build()
                                            )
                                            .build()
                                    )
                                    .build()
                            )
                            .build()
                    )
                    .addContent(
                        Text.Builder()
                            .setText("◀")
                            .setFontStyle(
                                FontStyle.Builder()
                                    .setSize(sp(16f))
                                    .setColor(argb(0xFF888888.toInt()))
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
            .addContent(
                // Next button
                Box.Builder()
                    .setWidth(dp(60f))
                    .setHeight(dp(30f))
                    .setModifiers(
                        Modifiers.Builder()
                            .setClickable(
                                Clickable.Builder()
                                    .setId("next_leg")
                                    .setOnClick(
                                        ActionBuilders.LaunchAction.Builder()
                                            .setAndroidActivity(
                                                ActionBuilders.AndroidActivity.Builder()
                                                    .setPackageName(context.packageName)
                                                    .setClassName("com.hsl.wear.tiles.TileActionReceiver")
                                                    .addKeyToExtraMapping(
                                                        TileActionReceiver.EXTRA_ACTION,
                                                        ActionBuilders.AndroidStringExtra.Builder()
                                                            .setValue(TileActionReceiver.ACTION_NEXT_LEG)
                                                            .build()
                                                    )
                                                    .build()
                                            )
                                            .build()
                                    )
                                    .build()
                            )
                            .build()
                    )
                    .addContent(
                        Text.Builder()
                            .setText("▶")
                            .setFontStyle(
                                FontStyle.Builder()
                                    .setSize(sp(16f))
                                    .setColor(argb(0xFF888888.toInt()))
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
            .build()
    }

    private fun transitLegContent(leg: Leg): LayoutElement {
        // Calculate arrival time ISO string for dynamic countdown
        val departureTime = TimeFormatter.parseIsoTime(leg.realtimeTimeIso ?: leg.scheduledTimeIso)
        val arrivalTimeMillis = departureTime + (leg.duration * 1000)
        val arrivalTimeIso = TimeFormatter.formatIsoTime(arrivalTimeMillis)

        // Row 1: Transport mode with platform - make mode explicit
        val modeName = when (leg.mode) {
            "BUS" -> "Bus"
            "TRAM" -> "Tram"
            "RAIL" -> "Train"
            "SUBWAY" -> "Metro"
            "FERRY" -> "Ferry"
            else -> leg.mode
        }
        val fullName = if (leg.line != null) {
            "$modeName ${leg.line}"
        } else {
            modeName
        }

        return Column.Builder()
            .addContent(
                Text.Builder()
                    .setText(fullName)
                    .setFontStyle(
                        FontStyle.Builder()
                            .setSize(sp(18f))
                            .setColor(argb(0xFFFFFFFF.toInt()))
                            .build()
                    )
                    .setMaxLines(1)
                    .build()
            )
            .addContent(
                Spacer.Builder()
                    .setHeight(dp(3f))
                    .build()
            )
            .addContent(
                createDynamicStatusText(
                    departureTimeIso = leg.realtimeTimeIso ?: leg.scheduledTimeIso,
                    platformCode = leg.fromPlatformCode,
                    headsign = leg.headsign,
                    fromStopName = leg.fromStopName,
                    mode = leg.mode
                )
            )
            .addContent(
                Spacer.Builder()
                    .setHeight(dp(3f))
                    .build()
            )
            .addContent(
                createDynamicStationName(
                    departureTimeIso = leg.realtimeTimeIso ?: leg.scheduledTimeIso,
                    fromStopName = leg.fromStopName,
                    toStopName = leg.toStopName
                )
            )
            .addContent(
                Spacer.Builder()
                    .setHeight(dp(2f))
                    .build()
            )
            .addContent(
                createDynamicCountdownText(
                    departureTimeIso = leg.realtimeTimeIso ?: leg.scheduledTimeIso,
                    arrivalTimeIso = arrivalTimeIso,
                    hasRealtimeData = leg.hasRealtimeData
                )
            )
            .build()
    }

    private fun noActiveLegContent(): LayoutElement {
        return Column.Builder()
            .addContent(
                Text.Builder()
                    .setText("No Active")
                    .setFontStyle(
                        FontStyle.Builder()
                            .setSize(sp(20f))
                            .setColor(argb(0xFFCCCCCC.toInt()))
                            .build()
                    )
                    .build()
            )
            .addContent(
                Spacer.Builder()
                    .setHeight(dp(4f))
                    .build()
            )
            .addContent(
                Text.Builder()
                    .setText("Route")
                    .setFontStyle(
                        FontStyle.Builder()
                            .setSize(sp(20f))
                            .setColor(argb(0xFFCCCCCC.toInt()))
                            .build()
                    )
                    .build()
            )
            .build()
    }

    private fun createDynamicStatusText(
        departureTimeIso: String,
        platformCode: String?,
        headsign: String?,
        fromStopName: String,
        mode: String
    ): LayoutElement {
        val departureEpochMillis = TimeFormatter.parseIsoTime(departureTimeIso)
        val departureInstant = Instant.ofEpochMilli(departureEpochMillis)

        val dynamicNow = DynamicInstant.platformTimeWithSecondsPrecision()
        val dynamicDeparture = DynamicInstant.withSecondsPrecision(departureInstant)

        // Check if boarded
        val secondsUntilDeparture = dynamicNow.durationUntil(dynamicDeparture).toIntSeconds()
        val isBoarded = secondsUntilDeparture.lte(0)

        // Before boarding status - special handling for ferries
        val beforeBoardingText = when {
            platformCode != null -> "Platform $platformCode"
            headsign != null -> "→ ${headsign.take(20)}"
            mode == "FERRY" -> "Ferry service"
            else -> fromStopName.take(20)
        }

        // Show platform/direction before boarding, "On board" after
        val statusText = DynamicString.onCondition(isBoarded)
            .use(DynamicString.constant("On board"))
            .elseUse(DynamicString.constant(beforeBoardingText))

        return Text.Builder()
            .setText(
                TypeBuilders.StringProp.Builder(beforeBoardingText)
                    .setDynamicValue(statusText)
                    .build()
            )
            .setLayoutConstraintsForDynamicText(
                TypeBuilders.StringLayoutConstraint.Builder("Platform 99        ")
                    .build()
            )
            .setFontStyle(
                FontStyle.Builder()
                    .setSize(sp(15f))
                    .setColor(argb(0xFFCCCCCC.toInt()))
                    .build()
            )
            .setMaxLines(1)
            .build()
    }

    private fun createDynamicStationName(
        departureTimeIso: String,
        fromStopName: String,
        toStopName: String
    ): LayoutElement {
        val departureEpochMillis = TimeFormatter.parseIsoTime(departureTimeIso)
        val departureInstant = Instant.ofEpochMilli(departureEpochMillis)

        val dynamicNow = DynamicInstant.platformTimeWithSecondsPrecision()
        val dynamicDeparture = DynamicInstant.withSecondsPrecision(departureInstant)

        // Check if boarded
        val secondsUntilDeparture = dynamicNow.durationUntil(dynamicDeparture).toIntSeconds()
        val isBoarded = secondsUntilDeparture.lte(0)

        // Allow longer station names (up to ~35 chars for 2 lines)
        val fromStation = if (fromStopName.length > 35) {
            fromStopName.take(33) + ".."
        } else {
            fromStopName
        }
        val toStation = if (toStopName.length > 35) {
            toStopName.take(33) + ".."
        } else {
            toStopName
        }

        // Show departure station before boarding, arrival station after
        val stationText = DynamicString.onCondition(isBoarded)
            .use(DynamicString.constant(toStation))
            .elseUse(DynamicString.constant(fromStation))

        return Text.Builder()
            .setText(
                TypeBuilders.StringProp.Builder(fromStation)
                    .setDynamicValue(stationText)
                    .build()
            )
            .setLayoutConstraintsForDynamicText(
                TypeBuilders.StringLayoutConstraint.Builder("Helsinki Central Railway\nStation Platform 1")
                    .build()
            )
            .setFontStyle(
                FontStyle.Builder()
                    .setSize(sp(17f))
                    .setColor(argb(0xFFFFFFFF.toInt()))
                    .build()
            )
            .setMaxLines(2)
            .build()
    }

    private fun createDynamicCountdownText(
        departureTimeIso: String,
        arrivalTimeIso: String,
        hasRealtimeData: Boolean
    ): LayoutElement {
        // Parse timestamps to Instant
        val departureEpochMillis = TimeFormatter.parseIsoTime(departureTimeIso)
        val arrivalEpochMillis = TimeFormatter.parseIsoTime(arrivalTimeIso)

        val departureInstant = Instant.ofEpochMilli(departureEpochMillis)
        val arrivalInstant = Instant.ofEpochMilli(arrivalEpochMillis)

        // Get system time (ticks automatically every second)
        val dynamicNow = DynamicInstant.platformTimeWithSecondsPrecision()

        // Create dynamic target times
        val dynamicDeparture = DynamicInstant.withSecondsPrecision(departureInstant)
        val dynamicArrival = DynamicInstant.withSecondsPrecision(arrivalInstant)

        // Calculate time UNTIL departure/arrival (for countdown display)
        val timeUntilDeparture = dynamicNow.durationUntil(dynamicDeparture)
        val timeUntilArrival = dynamicNow.durationUntil(dynamicArrival)

        // Extract TOTAL minutes for display
        val boardingMinutes = timeUntilDeparture.toIntMinutes()
        val arrivalMinutes = timeUntilArrival.toIntMinutes()

        // Use total seconds for state checks
        val secondsUntilDeparture = timeUntilDeparture.toIntSeconds()
        val secondsUntilArrival = timeUntilArrival.toIntSeconds()

        // Check journey state:
        // - Before boarding: secondsUntilDeparture > 0
        // - On board: secondsUntilDeparture <= 0 AND secondsUntilArrival > 0
        // - Completed: secondsUntilArrival <= 0
        val isBoarded = secondsUntilDeparture.lte(0)
        val isOnBoard = isBoarded.and(secondsUntilArrival.gt(0))
        val isComplete = secondsUntilArrival.lte(0)

        // Build conditional text based on journey state
        val finalText = DynamicString.onCondition(isComplete)
            .use(DynamicString.constant("Tap ↻ to refresh"))
            .elseUse(
                DynamicString.onCondition(isOnBoard)
                    .use(
                        DynamicString.onCondition(arrivalMinutes.lte(0))
                            .use(DynamicString.constant("Arriving now"))
                            .elseUse(
                                DynamicString.constant("Arrives in ")
                                    .concat(arrivalMinutes.format())
                                    .concat(DynamicString.constant(" min"))
                            )
                    )
                    .elseUse(
                        DynamicString.onCondition(boardingMinutes.lte(0))
                            .use(DynamicString.constant("Boarding now"))
                            .elseUse(
                                DynamicString.constant("Boards in ")
                                    .concat(boardingMinutes.format())
                                    .concat(DynamicString.constant(" min"))
                            )
                    )
            )

        // Color: blue if realtime data, white otherwise
        val textColor = if (hasRealtimeData) 0xFF0072C6.toInt() else 0xFFFFFFFF.toInt()

        // Build text element with dynamic expression
        return Text.Builder()
            .setText(
                TypeBuilders.StringProp.Builder("--")
                    .setDynamicValue(finalText)
                    .build()
            )
            .setLayoutConstraintsForDynamicText(
                TypeBuilders.StringLayoutConstraint.Builder("Arrives in 999 min")
                    .build()
            )
            .setFontStyle(
                FontStyle.Builder()
                    .setSize(sp(18f))
                    .setColor(argb(textColor))
                    .build()
            )
            .setMaxLines(1)
            .build()
    }

    private fun isRouteObsolete(routeState: RouteState): Boolean {
        val lastLeg = routeState.legs.lastOrNull() ?: return false
        val currentTime = System.currentTimeMillis()

        // Calculate final arrival time (last leg's start time + duration)
        val startTime = TimeFormatter.parseIsoTime(lastLeg.realtimeTimeIso ?: lastLeg.scheduledTimeIso)
        val finalArrivalTime = startTime + (lastLeg.duration * 1000)
        val autoEndTime = finalArrivalTime + (2 * 60 * 1000) // 2 minutes after arrival

        return currentTime >= autoEndTime
    }

    companion object {
        private const val RESOURCES_VERSION = "1"
        private const val TILE_SIZE = 120f
    }
}
