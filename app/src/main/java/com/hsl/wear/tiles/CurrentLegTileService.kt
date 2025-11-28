package com.hsl.wear.tiles

import android.content.Context
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
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
import com.hsl.wear.R
import com.hsl.wear.data.models.Leg
import com.hsl.wear.data.models.RouteState
import java.time.Instant
import com.hsl.wear.data.store.RouteStore
import com.hsl.wear.utils.TimeFormatter
import com.hsl.wear.tiles.components.TileNavigationButtons
import com.hsl.wear.tiles.components.TileRefreshButton
import com.hsl.wear.tiles.components.TileEmptyContent
import com.hsl.wear.tiles.components.DynamicTextHelper
import com.hsl.wear.tiles.components.TileTransitContent
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
                                if (transitLeg != null && routeState != null) {
                                    val totalLegs = routeState.legs.size
                                    TileTransitContent.transitLegContent(
                                        context = context,
                                        leg = transitLeg,
                                        legIndex = legIndex,
                                        totalLegs = totalLegs
                                    )
                                } else {
                                    TileEmptyContent.noActiveLegContent(context)
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
                        TileRefreshButton.createRefreshButton(context)
                    )
                    .build()
            )
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
