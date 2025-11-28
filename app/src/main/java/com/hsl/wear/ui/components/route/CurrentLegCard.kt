package com.hsl.wear.ui.components.route

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.*
import com.hsl.wear.R
import com.hsl.wear.data.models.Leg
import com.hsl.wear.ui.components.TransportModeIcon
import com.hsl.wear.ui.theme.HslBlue
import com.hsl.wear.utils.TimeFormatter

@Composable
fun CurrentLegCard(
    leg: Leg,
    currentTime: Long,
    isActive: Boolean,
    isLastLeg: Boolean = false,
    destinationName: String? = null
) {
    val context = LocalContext.current

    Card(
        onClick = { /* Handle card tap */ },
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Transport mode and line + platform/direction
            TransportModeSection(leg)
            Spacer(modifier = Modifier.height(16.dp))

            // Explicit departure countdown
            val statusMinutes = calculateStatusMinutes(leg, currentTime)
            Text(
                text = getStatusDisplayText(statusMinutes, leg.isWalking, context.resources),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = if (leg.hasRealtimeData) HslBlue else MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Station name
            Text(
                text = when {
                    leg.isWalking && isLastLeg && destinationName != null -> stringResource(R.string.walk_to_destination, destinationName)
                    leg.isWalking -> stringResource(R.string.walk_to_destination, leg.toStopName)
                    else -> stringResource(R.string.from_location, leg.fromStopName)
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Calculate arrival time for both walking and transit
            val (arrivalTime, arrivalMinutes) = calculateArrivalTimeInfo(leg, currentTime)

            // Distance and duration combined for walking, or number of stops for transit
            if (leg.isWalking) {
                WalkingInfoSection(leg, arrivalMinutes, context.resources)
            } else {
                TransitInfoSection(leg, arrivalMinutes)
            }
        }
    }
}

// Helper functions for this component
private fun calculateStatusMinutes(leg: Leg, currentTime: Long): Int =
    ((TimeFormatter.parseIsoTime(leg.realtimeTimeIso ?: leg.scheduledTimeIso) - currentTime) / (1000 * 60)).toInt()

private fun calculateArrivalTimeInfo(leg: Leg, currentTime: Long): Pair<Long, Int> {
    val startTime = TimeFormatter.parseIsoTime(leg.realtimeTimeIso ?: leg.scheduledTimeIso)
    val arrivalTime = startTime + (leg.duration * 1000)
    val arrivalMinutes = ((arrivalTime - currentTime) / (1000 * 60)).toInt()
    return Pair(arrivalTime, arrivalMinutes)
}

private fun getStatusDisplayText(statusMinutes: Int, isWalking: Boolean, resources: android.content.res.Resources): String = when {
    isWalking && statusMinutes > 0 -> resources.getString(R.string.start_walking_in, statusMinutes)
    isWalking -> resources.getString(R.string.on_route)
    statusMinutes > 0 -> resources.getString(R.string.departing_in, statusMinutes)
    statusMinutes == 0 -> resources.getString(R.string.departs_now)
    else -> resources.getString(R.string.departed)
}

private fun getArrivalDisplayText(arrivalMinutes: Int, resources: android.content.res.Resources): String = when {
    arrivalMinutes > 0 -> resources.getString(R.string.arrive_in, arrivalMinutes)
    arrivalMinutes == 0 -> resources.getString(R.string.arriving_now)
    else -> resources.getString(R.string.departed)
}

@Composable
private fun TransportModeSection(leg: Leg) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        TransportModeIcon(
            mode = leg.mode,
            size = 32.dp
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = leg.transportDisplayName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            // Platform or direction
            if (!leg.isWalking) {
                leg.fromPlatformCode?.let { platform ->
                    Text(
                        text = stringResource(R.string.platform, platform),
                        style = MaterialTheme.typography.labelSmall,
                        color = HslBlue
                    )
                }
                leg.headsign?.let { headsign ->
                    Text(
                        text = stringResource(R.string.direction, headsign),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.LightGray
                    )
                }
            }
        }
    }
}

@Composable
private fun WalkingInfoSection(leg: Leg, arrivalMinutes: Int, resources: android.content.res.Resources) {
    leg.distance?.let { distance ->
        Text(
            text = stringResource(R.string.distance_duration, distance, leg.duration / 60),
            style = MaterialTheme.typography.labelMedium,
            color = Color.LightGray
        )
    }

    Spacer(modifier = Modifier.height(4.dp))

    // Arrival time for walking
    Text(
        text = getArrivalDisplayText(arrivalMinutes, resources),
        style = MaterialTheme.typography.labelMedium,
        color = if (leg.hasRealtimeData) HslBlue else Color.LightGray
    )
}

@Composable
private fun TransitInfoSection(leg: Leg, arrivalMinutes: Int) {
    val numStops = leg.intermediateStops.size + 1
    Text(
        text = "${stringResource(if (numStops == 1) R.string.stop_count_one else R.string.stop_count_many, numStops)} | ${leg.duration / 60} min",
        style = MaterialTheme.typography.labelMedium,
        color = Color.LightGray
    )

    // Terminal station
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = stringResource(R.string.to_location, leg.toStopName),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center,
        maxLines = 3
    )

    // Arrival time for transit (after "To:" line)
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = getArrivalDisplayText(arrivalMinutes, LocalContext.current.resources),
        style = MaterialTheme.typography.labelMedium,
        color = if (leg.hasRealtimeData) HslBlue else Color.LightGray
    )
}