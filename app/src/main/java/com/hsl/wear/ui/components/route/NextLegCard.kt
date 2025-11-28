package com.hsl.wear.ui.components.route

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.*
import com.hsl.wear.R
import com.hsl.wear.data.models.Leg
import com.hsl.wear.ui.components.TransportModeIcon
import com.hsl.wear.ui.theme.HslBlue
import com.hsl.wear.utils.TimeFormatter

@Composable
fun NextLegCard(
    leg: Leg,
    isLastLeg: Boolean = false,
    destinationName: String? = null,
    onNextLeg: () -> Unit
) {
    Card(
        onClick = onNextLeg,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Header: "Next" and departure time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.next),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = TimeFormatter.formatTime(leg.scheduledTimeIso),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (leg.hasRealtimeData) HslBlue else MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Icon + line number + station + direction
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TransportModeIcon(
                    mode = leg.mode,
                    size = 24.dp
                )

                Column(modifier = Modifier.weight(1f)) {
                    // Line number or "Walk"
                    Text(
                        text = leg.transportDisplayName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    // Station/platform name
                    Text(
                        text = when {
                            leg.isWalking && isLastLeg && destinationName != null -> stringResource(R.string.walk_to_destination, destinationName)
                            leg.isWalking -> stringResource(R.string.walk_to_destination, leg.toStopName)
                            else -> leg.fromStopName
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        maxLines = 2
                    )

                    // Direction (headsign) without endpoint
                    if (!leg.isWalking && leg.headsign != null) {
                        Text(
                            text = stringResource(R.string.direction, leg.headsign),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}