package com.hsl.wear.ui.components.routetracking

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hsl.wear.data.models.Leg
import com.hsl.wear.ui.components.TransportModeIcon
import com.hsl.wear.ui.theme.HslBlue
import com.hsl.wear.utils.TimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrentLegCard(
    leg: Leg,
    currentTime: Long,
    isActive: Boolean,
    isLastLeg: Boolean = false,
    destinationName: String? = null
) {
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
                                text = "Platform $platform",
                                style = MaterialTheme.typography.labelSmall,
                                color = HslBlue
                            )
                        }
                        leg.headsign?.let { headsign ->
                            Text(
                                text = "→ $headsign",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.LightGray
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Explicit departure countdown
            val statusMinutes = ((TimeFormatter.parseIsoTime(leg.realtimeTimeIso ?: leg.scheduledTimeIso) - currentTime) / (1000 * 60)).toInt()
            Text(
                text = when {
                    leg.isWalking && statusMinutes > 0 -> "Start walking in $statusMinutes min"
                    leg.isWalking -> "On route"
                    statusMinutes > 0 -> "Departs in \n$statusMinutes min"
                    statusMinutes == 0 -> "Departing now"
                    else -> "Departed"
                },
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = if (leg.hasRealtimeData) HslBlue else MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Station name
            Text(
                text = when {
                    leg.isWalking && isLastLeg && destinationName != null -> "Walk to $destinationName"
                    leg.isWalking -> "Walk to ${leg.toStopName}"
                    else -> "From: ${leg.fromStopName}"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Calculate arrival time for both walking and transit
            val startTime = TimeFormatter.parseIsoTime(leg.realtimeTimeIso ?: leg.scheduledTimeIso)
            val arrivalTime = startTime + (leg.duration * 1000)
            val arrivalMinutes = ((arrivalTime - currentTime) / (1000 * 60)).toInt()

            // Distance and duration combined for walking, or number of stops for transit
            if (leg.isWalking) {
                leg.distance?.let { distance ->
                    Text(
                        text = "${distance} m | ${leg.duration / 60} min",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.LightGray
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Arrival time for walking
                Text(
                    text = when {
                        arrivalMinutes > 0 -> "Arrive in $arrivalMinutes min"
                        arrivalMinutes == 0 -> "Arriving now"
                        else -> "Arrived"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = if (leg.hasRealtimeData) HslBlue else Color.LightGray
                )
            } else {
                val numStops = leg.intermediateStops.size + 1
                Text(
                    text = "$numStops ${if (numStops == 1) "stop" else "stops"} | ${leg.duration / 60} min",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.LightGray
                )

                // Terminal station
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "To: ${leg.toStopName}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 3
                )

                // Arrival time for transit (after "To:" line)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = when {
                        arrivalMinutes > 0 -> "Arrive in $arrivalMinutes min"
                        arrivalMinutes == 0 -> "Arriving now"
                        else -> "Arrived"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = if (leg.hasRealtimeData) HslBlue else Color.LightGray
                )
            }
        }
    }
}