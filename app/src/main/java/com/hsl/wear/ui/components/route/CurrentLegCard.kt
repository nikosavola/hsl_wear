package com.hsl.wear.ui.components.route

import androidx.compose.foundation.layout.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.zIndex
import androidx.wear.compose.material3.*
import com.hsl.wear.R
import com.hsl.wear.data.models.Leg
import com.hsl.wear.ui.components.TransportModeIcon
import com.hsl.wear.ui.theme.HslBlue
import com.hsl.wear.utils.TimeFormatter
import com.hsl.wear.utils.JourneyState

@Composable
fun CurrentLegCard(
    leg: Leg,
    currentTime: Long,
    isLastLeg: Boolean = false,
    destinationName: String? = null,
    isRefreshing: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val context = LocalContext.current

    Card(
        onClick = onClick ?: {},
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Main content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(if (isRefreshing) 0.4f else 1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Transport mode and line + platform/direction
                TransportModeSection(leg)
            Spacer(modifier = Modifier.height(16.dp))

            // Show updated status text (departure-focused before boarding, arrival-focused after)
            Text(
                text = getStatusDisplayText(leg, currentTime, context.resources),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = if (leg.hasRealtimeData) HslBlue else MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Station name - smart display based on journey state
            Text(
                text = when {
                    leg.isWalking && isLastLeg && destinationName != null -> stringResource(R.string.walk_to_destination, destinationName)
                    leg.isWalking -> stringResource(R.string.walk_to_destination, leg.toStopName)
                    else -> {
                        val journeyState = TimeFormatter.calculateJourneyState(leg, currentTime)
                        when (journeyState) {
                            JourneyState.BEFORE_BOARDING -> {
                                // Pre-boarding: show departure location
                                stringResource(R.string.from_location, leg.fromStopName)
                            }
                            JourneyState.ON_BOARD, JourneyState.ARRIVED -> {
                                // On board or arrived: show destination
                                stringResource(R.string.exit_location, leg.toStopName)
                            }
                        }
                    }
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
                TransitInfoSection(leg, currentTime, arrivalMinutes)
            }
            }

            // Overlay loading indicator that doesn't cause layout shifts
            if (isRefreshing) {
                val infiniteTransition = rememberInfiniteTransition()
                val scale by infiniteTransition.animateFloat(
                    initialValue = 1.0f,
                    targetValue = 1.1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = EaseInOutCubic),
                        repeatMode = RepeatMode.Reverse
                    )
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .scale(scale)
                        .zIndex(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        strokeWidth = 3.dp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Getting real-time data...",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// Helper functions for this component
private fun calculateStatusMinutes(leg: Leg, currentTime: Long): Int =
    TimeFormatter.getTimeUntilDeparture(leg, currentTime)

private fun calculateArrivalTimeInfo(leg: Leg, currentTime: Long): Pair<Long, Int> {
    val departureTime = TimeFormatter.parseIsoTime(leg.realtimeTimeIso ?: leg.scheduledTimeIso)
    val arrivalTime = departureTime + (leg.duration * 1000)
    val arrivalMinutes = TimeFormatter.getTimeUntilArrival(leg, currentTime)
    return Pair(arrivalTime, arrivalMinutes)
}

private fun getStatusDisplayText(leg: Leg, currentTime: Long, resources: android.content.res.Resources): String = when {
    leg.isWalking -> {
        val statusMinutes = calculateStatusMinutes(leg, currentTime)
        when {
            statusMinutes > 0 -> resources.getString(R.string.start_walking_in, statusMinutes)
            else -> resources.getString(R.string.on_route)
        }
    }
    else -> {
        val journeyState = TimeFormatter.calculateJourneyState(leg, currentTime)
        when (journeyState) {
            JourneyState.BEFORE_BOARDING -> {
                val statusMinutes = calculateStatusMinutes(leg, currentTime)
                when {
                    statusMinutes > 0 -> resources.getString(R.string.departing_in, statusMinutes)
                    statusMinutes == 0 -> resources.getString(R.string.departs_now)
                    else -> resources.getString(R.string.departed)
                }
            }
            JourneyState.ON_BOARD -> {
                val arrivalMinutes = TimeFormatter.getTimeUntilArrival(leg, currentTime)
                when {
                    arrivalMinutes > 0 -> resources.getString(R.string.arrive_in, arrivalMinutes)
                    arrivalMinutes == 0 -> resources.getString(R.string.arriving_now)
                    else -> resources.getString(R.string.arrived)
                }
            }
            JourneyState.ARRIVED -> resources.getString(R.string.arrived)
        }
    }
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
private fun TransitInfoSection(leg: Leg, currentTime: Long, arrivalMinutes: Int) {
    val journeyState = TimeFormatter.calculateJourneyState(leg, currentTime)

    if (journeyState == JourneyState.BEFORE_BOARDING) {
        // Pre-boarding only: Show journey progress info and destination
        val numStops = leg.intermediateStops.size + 1
        Text(
            text = "${stringResource(if (numStops == 1) R.string.stop_count_one else R.string.stop_count_many, numStops)} | ${leg.duration / 60} min",
            style = MaterialTheme.typography.labelMedium,
            color = Color.LightGray
        )

        // Terminal station - changed from "To:" to "Exit:" for better readability
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.exit_location, leg.toStopName),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 3
        )

        // Arrival time (pre-boarding only)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = getArrivalDisplayText(arrivalMinutes, LocalContext.current.resources),
            style = MaterialTheme.typography.labelMedium,
            color = if (leg.hasRealtimeData) HslBlue else Color.LightGray
        )
    }
    // ON_BOARD or ARRIVED: No additional info needed - everything shown in main station name and timing text
}