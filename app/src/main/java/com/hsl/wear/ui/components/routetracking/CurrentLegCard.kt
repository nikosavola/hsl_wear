package com.hsl.wear.ui.components.routetracking

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
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
    // Skip walking legs - only show transit
    if (leg.isWalking) {
        return
    }

    val departureTime = TimeFormatter.parseIsoTime(leg.realtimeTimeIso ?: leg.scheduledTimeIso)
    val boardingMinutes = ((departureTime - currentTime) / (1000 * 60)).toInt()
    val isBoarded = boardingMinutes <= 0

    // Calculate arrival time at exit stop
    val arrivalTime = departureTime + (leg.duration * 1000)
    val exitMinutes = ((arrivalTime - currentTime) / (1000 * 60)).toInt()
    val numStops = leg.intermediateStops.size + 1

    Card(
        onClick = { /* Handle card tap */ },
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Transport mode icon and line
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                TransportModeIcon(
                    mode = leg.mode,
                    size = 40.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = leg.transportDisplayName,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    leg.fromPlatformCode?.let { platform ->
                        Text(
                            text = "Platform $platform",
                            style = MaterialTheme.typography.titleSmall,
                            color = HslBlue,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (!isBoarded) {
                // BEFORE BOARDING - Show station and boarding time
                Text(
                    text = leg.fromStopName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = when {
                        boardingMinutes > 0 -> "Boards in $boardingMinutes min"
                        boardingMinutes == 0 -> "Boarding now"
                        else -> "Departed ${-boardingMinutes} min ago"
                    },
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = if (leg.hasRealtimeData) HslBlue else MaterialTheme.colorScheme.onSurface
                )

                // Show direction/headsign
                leg.headsign?.let { headsign ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "→ $headsign",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.LightGray,
                        textAlign = TextAlign.Center
                    )
                }

                // Preview exit info
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Exit at: ${leg.toStopName}",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.LightGray,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
                Text(
                    text = "$numStops ${if (numStops == 1) "stop" else "stops"}",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.LightGray
                )

            } else {
                // AFTER BOARDING - Show exit info
                Text(
                    text = "On Board",
                    style = MaterialTheme.typography.titleLarge,
                    color = HslBlue,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Exit at: ${leg.toStopName}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = when {
                        exitMinutes > 0 -> "In $exitMinutes min"
                        exitMinutes == 0 -> "Exit now"
                        else -> "Passed"
                    },
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = if (leg.hasRealtimeData) HslBlue else MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "$numStops ${if (numStops == 1) "stop" else "stops"} remaining",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.LightGray
                )
            }
        }
    }
}
