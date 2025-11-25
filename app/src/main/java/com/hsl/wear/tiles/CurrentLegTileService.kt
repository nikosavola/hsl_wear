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
import androidx.wear.tiles.RequestBuilders.ResourcesRequest
import androidx.wear.tiles.RequestBuilders.TileRequest
import androidx.wear.tiles.TileBuilders.Tile
import androidx.wear.tiles.TileService
import com.hsl.wear.data.models.Leg
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

    override fun onTileRequest(requestParams: TileRequest): com.google.common.util.concurrent.ListenableFuture<Tile> {
        return com.google.common.util.concurrent.Futures.immediateFuture(
            runBlocking {
                val routeState = routeStore.routeStateFlow.first()

                // Find current/next relevant transit leg based on TIME (not currentIndex)
                val transitLeg = routeState?.legs?.let { legs ->
                    val currentTime = System.currentTimeMillis()

                    // Find first transit leg that hasn't ended yet
                    legs.filter { !it.isWalking }.firstOrNull { leg ->
                        val departureTime = TimeFormatter.parseIsoTime(leg.realtimeTimeIso ?: leg.scheduledTimeIso)
                        val arrivalTime = departureTime + (leg.duration * 1000)
                        // Show if not yet arrived (still relevant)
                        currentTime < arrivalTime
                    }
                }

                android.util.Log.d("CurrentLegTileService", "Tile request at ${System.currentTimeMillis()} - RouteState: ${routeState != null}, Transit Leg: ${transitLeg?.transportDisplayName}, Time-based selection")

                Tile.Builder()
                    .setResourcesVersion(RESOURCES_VERSION)
                    .setTileTimeline(
                        Timeline.Builder()
                            .addTimelineEntry(
                                TimelineEntry.Builder()
                                    .setLayout(
                                        LayoutElementBuilders.Layout.Builder()
                                            .setRoot(
                                                tileLayout(
                                                    context = this@CurrentLegTileService,
                                                    transitLeg = transitLeg
                                                )
                                            )
                                            .build()
                                    )
                                    .build()
                            )
                            .build()
                    )
                    .setFreshnessIntervalMillis(10_000) // Update every 10 seconds for fresher data
                    .build()
            }
        )
    }

    override fun onTileResourcesRequest(requestParams: ResourcesRequest): com.google.common.util.concurrent.ListenableFuture<Resources> {
        return com.google.common.util.concurrent.Futures.immediateFuture(
            Resources.Builder()
                .setVersion(RESOURCES_VERSION)
                .build()
        )
    }

    private fun tileLayout(
        context: Context,
        transitLeg: Leg?
    ): LayoutElement {
        // Full tile is clickable to open app - no separate buttons
        return Box.Builder()
            .setWidth(dp(TILE_SIZE))
            .setHeight(dp(TILE_SIZE))
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
        val currentTime = System.currentTimeMillis()
        val departureTime = TimeFormatter.parseIsoTime(leg.realtimeTimeIso ?: leg.scheduledTimeIso)
        val boardingMinutes = ((departureTime - currentTime) / (1000 * 60)).toInt()

        // Calculate arrival time at exit stop
        val arrivalTime = departureTime + (leg.duration * 1000)
        val exitMinutes = ((arrivalTime - currentTime) / (1000 * 60)).toInt()

        val isBoarded = boardingMinutes <= 0

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

        // Row 1: Just the transport name
        val row1 = fullName

        // Row 2: Only show platform/direction BEFORE boarding
        val row2 = if (!isBoarded) {
            // Before boarding: show platform or direction
            when {
                leg.fromPlatformCode != null -> "Platform ${leg.fromPlatformCode}"
                leg.headsign != null -> "→ ${leg.headsign.take(20)}"
                else -> leg.fromStopName.take(20)
            }
        } else {
            // After boarding: simple status
            "On board"
        }

        // Row 3: Station name (white) and time (blue if realtime)
        val stationName = if (!isBoarded) {
            if (leg.fromStopName.length > 18) {
                leg.fromStopName.take(16) + ".."
            } else {
                leg.fromStopName
            }
        } else {
            if (leg.toStopName.length > 18) {
                leg.toStopName.take(16) + ".."
            } else {
                leg.toStopName
            }
        }

        val timeText = if (!isBoarded) {
            // Before boarding: boarding time
            when {
                boardingMinutes > 0 -> "Boards in $boardingMinutes min"
                boardingMinutes == 0 -> "Boarding now"
                else -> "Departed"
            }
        } else {
            // After boarding: exit time
            when {
                exitMinutes > 0 -> "in $exitMinutes min"
                exitMinutes == 0 -> "Exit now"
                else -> "Passed"
            }
        }

        return Column.Builder()
            .addContent(
                Text.Builder()
                    .setText(row1)
                    .setFontStyle(
                        FontStyle.Builder()
                            .setSize(sp(15f))
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
                Text.Builder()
                    .setText(row2)
                    .setFontStyle(
                        FontStyle.Builder()
                            .setSize(sp(13f))
                            .setColor(argb(0xFFCCCCCC.toInt()))
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
                // Station name - always white, truncated with ".." if too long
                Text.Builder()
                    .setText(stationName)
                    .setFontStyle(
                        FontStyle.Builder()
                            .setSize(sp(15f))
                            .setColor(argb(0xFFFFFFFF.toInt()))
                            .build()
                    )
                    .setMaxLines(1)
                    .build()
            )
            .apply {
                // Time text - blue if realtime data available
                timeText?.let { time ->
                    addContent(
                        Spacer.Builder()
                            .setHeight(dp(2f))
                            .build()
                    )
                    addContent(
                        Text.Builder()
                            .setText(time)
                            .setFontStyle(
                                FontStyle.Builder()
                                    .setSize(sp(16f))
                                    .setColor(argb(if (leg.hasRealtimeData) 0xFF0072C6.toInt() else 0xFFFFFFFF.toInt()))
                                    .build()
                            )
                            .setMaxLines(1)
                            .build()
                    )
                }
            }
            .build()
    }

    private fun noActiveLegContent(): LayoutElement {
        return Column.Builder()
            .addContent(
                Text.Builder()
                    .setText("No Active")
                    .setFontStyle(
                        FontStyle.Builder()
                            .setSize(sp(18f))
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
                            .setSize(sp(18f))
                            .setColor(argb(0xFFCCCCCC.toInt()))
                            .build()
                    )
                    .build()
            )
            .build()
    }

    companion object {
        private const val RESOURCES_VERSION = "1"
        private const val TILE_SIZE = 120f
    }
}
