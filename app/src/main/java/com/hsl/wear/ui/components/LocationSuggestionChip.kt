package com.hsl.wear.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.*

@Composable
fun LocationSuggestionChip(
    text: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon?.let { icon ->
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                subtitle?.let { subtitle ->
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = MaterialTheme.typography.labelSmall.fontSize * 0.8f
                        ),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun FavoriteLocationChip(
    locationName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LocationSuggestionChip(
        text = locationName,
        icon = Icons.Default.Star,
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
fun RecentLocationChip(
    locationName: String,
    timeAgo: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LocationSuggestionChip(
        text = locationName,
        subtitle = timeAgo,
        icon = Icons.Default.Info,
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
fun NearbyLocationChip(
    locationName: String,
    distance: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LocationSuggestionChip(
        text = locationName,
        subtitle = distance,
        icon = Icons.Default.LocationOn,
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
fun HomeLocationChip(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LocationSuggestionChip(
        text = "Home",
        icon = Icons.Default.Home,
        onClick = onClick,
        modifier = modifier
    )
}

enum class SuggestionType {
    FAVORITE,
    RECENT,
    NEARBY,
    HOME,
    CURRENT
}