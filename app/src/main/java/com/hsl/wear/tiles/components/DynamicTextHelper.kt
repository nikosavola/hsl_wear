package com.hsl.wear.tiles.components

import android.content.Context
import androidx.wear.protolayout.ColorBuilders.argb
import androidx.wear.protolayout.DimensionBuilders.dp
import androidx.wear.protolayout.DimensionBuilders.sp
import androidx.wear.protolayout.LayoutElementBuilders.FontStyle
import androidx.wear.protolayout.LayoutElementBuilders.LayoutElement
import androidx.wear.protolayout.LayoutElementBuilders.Text
import androidx.wear.protolayout.TypeBuilders
import androidx.wear.protolayout.expression.DynamicBuilders
import androidx.wear.protolayout.expression.DynamicBuilders.DynamicInstant
import androidx.wear.protolayout.expression.DynamicBuilders.DynamicString
import com.hsl.wear.R
import com.hsl.wear.data.models.Leg
import com.hsl.wear.utils.TimeFormatter
import java.time.Instant

object DynamicTextHelper {
    fun createDynamicStatusText(
        context: Context,
        departureTimeIso: String,
        platformCode: String?,
        headsign: String?,
        fromStopName: String,
        mode: String,
        leg: Leg
    ): LayoutElement {
        val departureEpochMillis = TimeFormatter.parseIsoTime(departureTimeIso)
        val departureInstant = Instant.ofEpochMilli(departureEpochMillis)
        val arrivalEpochMillis = departureEpochMillis + (leg.duration * 1000)
        val arrivalInstant = Instant.ofEpochMilli(arrivalEpochMillis)

        val dynamicNow = DynamicInstant.platformTimeWithSecondsPrecision()
        val dynamicDeparture = DynamicInstant.withSecondsPrecision(departureInstant)
        val dynamicArrival = DynamicInstant.withSecondsPrecision(arrivalInstant)

        // Check if boarded
        val secondsUntilDeparture = dynamicNow.durationUntil(dynamicDeparture).toIntSeconds()
        val secondsUntilArrival = dynamicNow.durationUntil(dynamicArrival).toIntSeconds()
        val isBoarded = secondsUntilDeparture.lte(0)

        // Check if arrived (within 2 minutes of arrival)
        val secondsSinceArrival = dynamicArrival.durationUntil(dynamicNow).toIntSeconds()
        val isArrived = secondsUntilArrival.lte(0)

        // Before boarding status - special handling for ferries
        val beforeBoardingText = when {
            platformCode != null -> context.getString(R.string.platform, platformCode)
            headsign != null -> context.getString(R.string.direction, headsign.take(20))
            mode == "FERRY" -> context.getString(R.string.ferry) + " service"
            else -> fromStopName.take(20)
        }

        // Show platform/direction before boarding, "On board" after boarding, "Arrived" after arrival
        val statusText = DynamicString.onCondition(isArrived)
            .use(DynamicString.constant(context.getString(R.string.arrived)))
            .elseUse(
                DynamicString.onCondition(isBoarded)
                    .use(DynamicString.constant(context.getString(R.string.on_board)))
                    .elseUse(DynamicString.constant(beforeBoardingText))
            )

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
                androidx.wear.protolayout.LayoutElementBuilders.FontStyle.Builder()
                    .setSize(sp(15f))
                    .setColor(argb(0xFFCCCCCC.toInt()))
                    .build()
            )
            .setMaxLines(1)
            .build()
    }

    fun createDynamicStationName(
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
                androidx.wear.protolayout.LayoutElementBuilders.FontStyle.Builder()
                    .setSize(sp(17f))
                    .setColor(argb(0xFFFFFFFF.toInt()))
                    .build()
            )
            .setMaxLines(2)
            .build()
    }

    fun createDynamicCountdownText(
        context: Context,
        departureTimeIso: String,
        arrivalTimeIso: String,
        hasRealtimeData: Boolean
    ): LayoutElement {
        // Validate time strings before parsing
        if (departureTimeIso.isEmpty() || arrivalTimeIso.isEmpty()) {
            return Text.Builder()
                .setText(context.getString(R.string.tap_to_refresh))
                .setFontStyle(
                    androidx.wear.protolayout.LayoutElementBuilders.FontStyle.Builder()
                        .setSize(sp(18f))
                        .setColor(argb(0xFF888888.toInt()))
                        .build()
                )
                .setMaxLines(1)
                .build()
        }

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
            .use(DynamicString.constant(context.getString(R.string.tap_to_refresh)))
            .elseUse(
                DynamicString.onCondition(isOnBoard)
                    .use(
                        DynamicString.onCondition(arrivalMinutes.lte(0))
                            .use(DynamicString.constant(context.getString(R.string.arriving_now)))
                            .elseUse(
                                DynamicString.constant(context.getString(R.string.arrives_in_prefix))
                                    .concat(DynamicString.constant(" "))
                                    .concat(arrivalMinutes.format())
                                    .concat(DynamicString.constant(" "))
                                    .concat(DynamicString.constant(context.getString(R.string.minutes_suffix)))
                            )
                    )
                    .elseUse(
                        DynamicString.onCondition(boardingMinutes.lte(0))
                            .use(DynamicString.constant(context.getString(R.string.boarding_now)))
                            .elseUse(
                                DynamicString.constant(context.getString(R.string.boards_in_prefix))
                                    .concat(DynamicString.constant(" "))
                                    .concat(boardingMinutes.format())
                                    .concat(DynamicString.constant(" "))
                                    .concat(DynamicString.constant(context.getString(R.string.minutes_suffix)))
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
                androidx.wear.protolayout.LayoutElementBuilders.FontStyle.Builder()
                    .setSize(sp(18f))
                    .setColor(argb(textColor))
                    .build()
            )
            .setMaxLines(1)
            .build()
    }
}