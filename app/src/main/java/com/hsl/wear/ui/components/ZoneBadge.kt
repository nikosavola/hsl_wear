package com.hsl.wear.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.wear.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hsl.wear.ui.theme.HslBlue
import com.hsl.wear.utils.ZoneUtils
import com.hsl.wear.utils.constants.ZoneConstants

/**
 * Zone badge component for displaying HSL fare zone information.
 * Optimized for Wear OS with clear text-based indicators.
 */
@Composable
fun ZoneBadge(
    zoneId: String?,
    modifier: Modifier = Modifier,
    showLabel: Boolean = false
) {
    val zoneText = ZoneUtils.getStopZoneIndicator(zoneId)

    if (zoneText != "?") {
        if (showLabel) {
            // Full badge with "Zone" label
            Text(
                text = "Zone $zoneText",
                modifier = modifier
                    .background(
                        color = HslBlue.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                color = HslBlue,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        } else {
            // Compact zone indicator
            Text(
                text = zoneText,
                modifier = modifier
                    .background(
                        color = HslBlue.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(3.dp)
                    )
                    .padding(horizontal = 4.dp, vertical = 1.dp),
                color = HslBlue,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Route zones display showing departure and arrival zones.
 * Shows single zone or zone transition for multi-zone routes.
 */
@Composable
fun RouteZonesDisplay(
    fromZoneId: String?,
    toZoneId: String?,
    modifier: Modifier = Modifier,
    showTransition: Boolean = true
) {
    val zoneText = if (showTransition) {
        ZoneUtils.formatRouteZones(fromZoneId, toZoneId)
    } else {
        ZoneUtils.getStopZoneIndicator(fromZoneId)
    }

    if (zoneText.isNotEmpty()) {
        Text(
            text = zoneText,
            modifier = modifier
                .background(
                    color = if (ZoneUtils.crossesMultipleZones(fromZoneId, toZoneId)) {
                        HslBlue.copy(alpha = 0.2f)
                    } else {
                        HslBlue.copy(alpha = 0.1f)
                    },
                    shape = RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 6.dp, vertical = 2.dp),
            color = HslBlue,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

