package com.hsl.wear.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.TimeTextDefaults
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material3.*
import com.hsl.wear.R
import com.hsl.wear.data.models.FavoriteRoute
import com.hsl.wear.ui.theme.HslBlue
import com.hsl.wear.ui.viewmodel.FavouriteRoutesViewModel

@Composable
fun FavouriteRoutesScreen(
    viewModel: FavouriteRoutesViewModel,
    onRouteClick: (FavoriteRoute) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val favoriteRoutes by viewModel.favoriteRoutes.collectAsState(initial = emptyList())

    val listState = rememberScalingLazyListState()

    Scaffold(
        timeText = {
            TimeText(timeSource = TimeTextDefaults.timeSource(TimeTextDefaults.timeFormat()))
        },
        positionIndicator = {
            if (favoriteRoutes.isNotEmpty()) {
                PositionIndicator(scalingLazyListState = listState)
            }
        }
    ) {
        Column(
            modifier = modifier.fillMaxSize()
        ) {
            // Header
            Text(
                text = stringResource(R.string.favourite_routes),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            )

            if (favoriteRoutes.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_favourite_routes),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                // List of favorite routes
                ScalingLazyColumn(
                    modifier = Modifier.weight(1f),
                    state = listState,
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    anchorType = androidx.wear.compose.foundation.lazy.ScalingLazyListAnchorType.ItemStart,
                    autoCentering = null
                ) {
                    favoriteRoutes.forEach { route ->
                        item {
                            FavoriteRouteCard(
                                route = route,
                                onClick = { onRouteClick(route) },
                                onDelete = {
                                    viewModel.removeFavoriteRoute(route.id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoriteRouteCard(
    route: FavoriteRoute,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Route name
                Text(
                    text = route.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = HslBlue
                )

                Spacer(modifier = Modifier.height(4.dp))

                // From location
                Text(
                    text = stringResource(R.string.from_location, route.fromLocation.name),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(2.dp))

                // To location
                Text(
                    text = stringResource(R.string.to_location, route.toLocation.name),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 1
                )
            }

            // Delete button
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete_favourite),
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
