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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hsl.wear.data.models.Leg
import com.hsl.wear.ui.components.TransportModeIcon
import com.hsl.wear.ui.theme.HslBlue
import com.hsl.wear.utils.TimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
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
            containerColor = MaterialTheme.colorScheme.surfaceVariant
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
                    text = "Next",
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
                            leg.isWalking && isLastLeg && destinationName != null -> "Walk to $destinationName"
                            leg.isWalking -> "Walk to ${leg.toStopName}"
                            else -> leg.fromStopName
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        maxLines = 2
                    )

                    // Direction (headsign) without endpoint
                    if (!leg.isWalking && leg.headsign != null) {
                        Text(
                            text = "→ ${leg.headsign}",
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