package com.hsl.wear.tiles.components

import android.content.Context
import androidx.wear.protolayout.ColorBuilders.argb
import androidx.wear.protolayout.DimensionBuilders.dp
import androidx.wear.protolayout.DimensionBuilders.sp
import androidx.wear.protolayout.LayoutElementBuilders.Column
import androidx.wear.protolayout.LayoutElementBuilders.FontStyle
import androidx.wear.protolayout.LayoutElementBuilders.LayoutElement
import androidx.wear.protolayout.LayoutElementBuilders.Spacer
import androidx.wear.protolayout.LayoutElementBuilders.Text
import com.hsl.wear.R
import com.hsl.wear.data.models.Leg
import com.hsl.wear.utils.TimeFormatter

object TileTransitContent {
    fun transitLegContent(context: Context, leg: Leg): LayoutElement {
        // Calculate arrival time ISO string for dynamic countdown
        val departureTime = TimeFormatter.parseIsoTime(leg.realtimeTimeIso ?: leg.scheduledTimeIso)
        val arrivalTimeMillis = departureTime + (leg.duration * 1000)
        val arrivalTimeIso = TimeFormatter.formatIsoTime(arrivalTimeMillis)

        // Row 1: Transport mode with platform - make mode explicit
        val modeName = when (leg.mode) {
            "BUS" -> context.getString(R.string.bus)
            "TRAM" -> context.getString(R.string.tram)
            "RAIL" -> context.getString(R.string.train)
            "SUBWAY" -> context.getString(R.string.metro)
            "FERRY" -> context.getString(R.string.ferry)
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
                DynamicTextHelper.createDynamicStatusText(
                    context = context,
                    departureTimeIso = leg.realtimeTimeIso ?: leg.scheduledTimeIso,
                    platformCode = leg.fromPlatformCode,
                    headsign = leg.headsign,
                    fromStopName = leg.fromStopName,
                    mode = leg.mode,
                    leg = leg
                )
            )
            .addContent(
                Spacer.Builder()
                    .setHeight(dp(3f))
                    .build()
            )
            .addContent(
                DynamicTextHelper.createDynamicStationName(
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
                DynamicTextHelper.createDynamicCountdownText(
                    context = context,
                    departureTimeIso = leg.realtimeTimeIso ?: leg.scheduledTimeIso,
                    arrivalTimeIso = arrivalTimeIso,
                    hasRealtimeData = leg.hasRealtimeData
                )
            )
            .build()
    }
}