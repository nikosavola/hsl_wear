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
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import androidx.wear.compose.material3.*
import com.hsl.wear.R
import com.hsl.wear.data.models.FavoriteRoute
import com.hsl.wear.ui.theme.HslBlue
import com.hsl.wear.ui.viewmodel.FavouriteRoutesViewModel

@Composable
fun FavouriteRoutesScreen(
    viewModel: FavouriteRoutesViewModel,
    onRouteClick: (FavoriteRoute) -> Unit,
    modifier: Modifier = Modifier
) {
    val favoriteRoutes by viewModel.favoriteRoutes.collectAsState(initial = emptyList())

    val listState = rememberTransformingLazyColumnState()
    val transformSpec = rememberTransformationSpec()

    ScreenScaffold(scrollState = listState) { contentPadding ->
        if (favoriteRoutes.isEmpty()) {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(contentPadding),
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
            TransformingLazyColumn(
                modifier = modifier.fillMaxSize(),
                state = listState,
                contentPadding = contentPadding,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        text = stringResource(R.string.favourite_routes),
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .transformedHeight(this@item, transformSpec)
                    )
                }
                favoriteRoutes.forEach { route ->
                    item {
                        FavoriteRouteCard(
                            route = route,
                            onClick = { onRouteClick(route) },
                            onDelete = { viewModel.removeFavoriteRoute(route.id) },
                            transformation = SurfaceTransformation(transformSpec),
                            modifier = Modifier.transformedHeight(this@item, transformSpec)
                        )
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
    onDelete: () -> Unit,
    transformation: SurfaceTransformation,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        transformation = transformation,
        modifier = modifier.fillMaxWidth(),
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
