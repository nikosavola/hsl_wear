package com.hsl.wear.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hsl.wear.utils.constants.TimeConstants
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.TimeTextDefaults
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material3.*
import com.hsl.wear.R
import com.hsl.wear.data.models.Itinerary
import com.hsl.wear.ui.components.TransportModeIcon
import com.hsl.wear.ui.components.RouteZonesDisplay
import com.hsl.wear.ui.components.ZoneBadge
import com.hsl.wear.ui.models.RouteSelectionUiState
import com.hsl.wear.ui.theme.HslBlue
import com.hsl.wear.ui.viewmodel.RouteSelectionViewModel

@Composable
fun RouteSelectionScreen(
    viewModel: RouteSelectionViewModel,
    routes: List<Itinerary>,
    isLoading: Boolean = false,
    fromLocation: com.hsl.wear.data.models.Location? = null,
    toLocation: com.hsl.wear.data.models.Location? = null,
    onNavigateBack: () -> Unit,
    onNavigationStarted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    // Only reset navigation state if it was previously set to true (coming back from navigation)
    LaunchedEffect(uiState.navigationStarted) {
        if (uiState.navigationStarted) {
            viewModel.resetNavigationState()
        }
    }

    // Only load routes if they're different from what's already loaded
    LaunchedEffect(routes.size, isLoading) {
        if (routes != uiState.availableRoutes || isLoading != uiState.isLoading) {
            viewModel.loadRoutes(routes, isLoading)
        }
    }

    LaunchedEffect(uiState.navigationStarted) {
        if (uiState.navigationStarted) {
            onNavigationStarted()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            // Handle error display
            viewModel.clearError()
        }
    }

    RouteSelectionScreenContent(
        uiState = uiState,
        fromLocation = fromLocation,
        toLocation = toLocation,
        onRouteSelected = { itinerary ->
            viewModel.selectRoute(itinerary, fromLocation, toLocation)
        },
        onNavigateBack = onNavigateBack,
        modifier = modifier
    )
}

@Composable
private fun RouteSelectionScreenContent(
    uiState: RouteSelectionUiState,
    fromLocation: com.hsl.wear.data.models.Location? = null,
    toLocation: com.hsl.wear.data.models.Location? = null,
    onRouteSelected: (Itinerary) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberScalingLazyListState()

    // Show loading state immediately to prevent "No routes found" flash
    // Debounce only applies to hiding the loading spinner
    var showLoading by remember { mutableStateOf(false) }
    var hideLoadingDelayed by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isLoading) {
        if (uiState.isLoading) {
            showLoading = true
            hideLoadingDelayed = false
        } else {
            // Delay hiding loading to prevent flash
            kotlinx.coroutines.delay(TimeConstants.SEARCH_DEBOUNCE_MS)
            hideLoadingDelayed = true
        }
    }

    Scaffold(
        timeText = {
            TimeText(timeSource = TimeTextDefaults.timeSource(TimeTextDefaults.timeFormat()))
        },
        positionIndicator = {
            if (!showLoading && uiState.availableRoutes.isNotEmpty()) {
                PositionIndicator(scalingLazyListState = listState)
            }
        }
    ) {
        if (showLoading && !hideLoadingDelayed) {
            LoadingContent()
        } else if (uiState.availableRoutes.isEmpty()) {
            EmptyRoutesContent(onNavigateBack = onNavigateBack)
        } else {
            RoutesListContent(
                routes = uiState.availableRoutes,
                onRouteSelected = onRouteSelected,
                onNavigateBack = onNavigateBack,
                listState = listState,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun RouteSelectionHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.select_route),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.loading_routes),
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun EmptyRoutesContent(onNavigateBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.no_routes_found),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onNavigateBack,
            colors = ButtonDefaults.filledTonalButtonColors()
        ) {
            Text(stringResource(R.string.retry))
        }
    }
}

@Composable
private fun RoutesListContent(
    routes: List<Itinerary>,
    onRouteSelected: (Itinerary) -> Unit,
    onNavigateBack: () -> Unit,
    listState: androidx.wear.compose.foundation.lazy.ScalingLazyListState,
    modifier: Modifier = Modifier
) {
    ScalingLazyColumn(
        modifier = modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        anchorType = androidx.wear.compose.foundation.lazy.ScalingLazyListAnchorType.ItemStart,
        autoCentering = null
    ) {
        item {
            RouteSelectionHeader()
        }

        routes.forEachIndexed { index, itinerary ->
            item {
                RouteCard(
                    itinerary = itinerary,
                    routeNumber = index + 1,
                    onSelect = { onRouteSelected(itinerary) }
                )
            }
        }
    }
}

@Composable
private fun RouteCard(
    itinerary: Itinerary,
    routeNumber: Int,
    onSelect: () -> Unit,
    viewModel: RouteSelectionViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    Card(
        onClick = onSelect,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            // Top row: Departure time, travel time, and zones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Departure time - red if it has passed
                val departureTime = com.hsl.wear.utils.TimeFormatter.parseIsoTime(itinerary.startTimeIso)
                val hasDeparted = departureTime < System.currentTimeMillis()

                Text(
                    text = viewModel.formatStartTime(itinerary.startTimeIso),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (hasDeparted) androidx.compose.ui.graphics.Color.Red
                           else MaterialTheme.colorScheme.onSurface
                )

                // Zone information - find first and last transit legs (skip walking)
                val firstTransitLeg = itinerary.legs.find { it.mode != "WALK" }
                val lastTransitLeg = itinerary.legs.findLast { it.mode != "WALK" }
                if (firstTransitLeg?.fromZoneId != null && lastTransitLeg?.toZoneId != null) {
                    RouteZonesDisplay(
                        fromZoneId = firstTransitLeg.fromZoneId,
                        toZoneId = lastTransitLeg.toZoneId,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                // Total travel time - emphasized like departure time
                Text(
                    text = viewModel.formatDuration(itinerary.totalMinutes),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Multi-row itinerary showing each leg
            ItineraryLegsColumn(legs = itinerary.legs, viewModel = viewModel)
        }
    }
}

@Composable
private fun ItineraryLegsColumn(
    legs: List<com.hsl.wear.data.models.Leg>,
    viewModel: RouteSelectionViewModel
) {
    // Filter out very short walks (less than 100m)
    val significantLegs = legs.filter { leg ->
        leg.mode != "WALK" || (leg.distance ?: 0) > 100
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        significantLegs.forEach { leg ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // First column: Icon + Transport type + Line number
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.width(110.dp)
                ) {
                    TransportModeIcon(
                        mode = leg.mode,
                        size = 16.dp
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    if (leg.mode == "WALK") {
                        Text(
                            text = stringResource(R.string.walk),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    } else {
                        // Transport mode name
                        val modeName = when (leg.mode) {
                            "BUS" -> stringResource(R.string.bus)
                            "TRAM" -> stringResource(R.string.tram)
                            "RAIL" -> stringResource(R.string.train)
                            "SUBWAY" -> stringResource(R.string.metro)
                            "FERRY" -> stringResource(R.string.ferry)
                            else -> leg.mode
                        }
                        Text(
                            text = modeName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = HslBlue
                        )

                        // Line number
                        leg.line?.let { line ->
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = line,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = HslBlue
                            )
                        }
                    }
                }

                // Second column: Duration/stops info (centered position)
                if (leg.mode == "WALK") {
                    // For walking: show duration in minutes
                    Text(
                        text = viewModel.formatLegDuration(leg.duration),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                } else {
                    // For transit: show number of stops
                    val numStops = leg.intermediateStops.size + 1
                    Text(
                        text = stringResource(
                            if (numStops == 1) R.string.stop_count_one else R.string.stop_count_many,
                            numStops
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}
