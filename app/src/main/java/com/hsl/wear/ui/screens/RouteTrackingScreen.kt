package com.hsl.wear.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.TimeTextDefaults
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material3.*
import kotlinx.coroutines.launch
import com.hsl.wear.R
import com.hsl.wear.data.models.Leg
import com.hsl.wear.ui.components.TransportModeIcon
import com.hsl.wear.ui.models.RouteTrackingUiState
import com.hsl.wear.ui.theme.HslBlue
import com.hsl.wear.ui.viewmodel.RouteTrackingViewModel
import com.hsl.wear.utils.TimeFormatter

@Composable
fun RouteTrackingScreen(
    viewModel: RouteTrackingViewModel,
    onNavigationEnded: () -> Unit,
    onBackToRouteSelection: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.navigationEnded) {
        if (uiState.navigationEnded) {
            onNavigationEnded()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            // Handle error display
            viewModel.clearError()
        }
    }

    val routeState = uiState.routeState
    if (uiState.hasActiveRoute && routeState != null) {
        RouteTrackingScreenContent(
            uiState = uiState,
            routeState = routeState,
            onPreviousLeg = viewModel::moveToPreviousLeg,
            onNextLeg = viewModel::moveToNextLeg,
            onEndNavigation = viewModel::endNavigation,
            onRefresh = viewModel::refreshRoute,
            onBackToRouteSelection = onBackToRouteSelection,
            onSaveToFavourites = viewModel::saveRouteAsFavorite,
            modifier = modifier
        )
    } else {
        NoActiveRouteScreen()
    }
}

@Composable
private fun RouteTrackingScreenContent(
    uiState: RouteTrackingUiState,
    routeState: com.hsl.wear.data.models.RouteState,
    onPreviousLeg: () -> Unit,
    onNextLeg: () -> Unit,
    onEndNavigation: () -> Unit,
    onRefresh: () -> Unit,
    onBackToRouteSelection: () -> Unit,
    onSaveToFavourites: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Add an extra page at the beginning (page 0) for back navigation
    // Page 0 = Back page, Pages 1-N = Legs 0-(N-1)
    val pagerState = rememberPagerState(
        initialPage = routeState.currentIndex + 1, // Offset by 1
        pageCount = { routeState.legs.size + 1 }
    )
    val coroutineScope = rememberCoroutineScope()

    // Ensure we start at the correct page on initial load
    LaunchedEffect(Unit) {
        val targetPage = routeState.currentIndex + 1
        if (pagerState.currentPage != targetPage) {
            pagerState.scrollToPage(targetPage)
        }
    }

    // Sync pager with route state changes (but don't interfere with back navigation on page 0)
    LaunchedEffect(routeState.currentIndex) {
        val targetPage = routeState.currentIndex + 1 // Offset by 1
        if (pagerState.currentPage != targetPage && pagerState.currentPage != 0) {
            pagerState.animateScrollToPage(targetPage)
        }
    }

    // Handle page 0 (back navigation page)
    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage == 0) {
            onBackToRouteSelection()
        }
    }

    Scaffold(
        timeText = {
            TimeText(timeSource = TimeTextDefaults.timeSource(TimeTextDefaults.timeFormat()))
        }
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = modifier.fillMaxSize(),
            userScrollEnabled = true
        ) { page ->
            // Page 0 = Back indicator, Pages 1+ = Legs
            if (page == 0) {
                // Back navigation page - show a simple indicator
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "← Back",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            } else {
                val legIndex = page - 1
                val leg = routeState.legs.getOrNull(legIndex)
                val nextLeg = routeState.legs.getOrNull(legIndex + 1)
                val lazyListState = rememberLazyListState()

                if (leg != null) {
                Box(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = lazyListState,
                        contentPadding = PaddingValues(start = 8.dp, end = 8.dp, top = 0.dp, bottom = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                    // Current leg with hero-style countdown
                    item {
                        CurrentLegCard(
                            leg = leg,
                            currentTime = uiState.currentTime,
                            isActive = true,
                            isLastLeg = legIndex == routeState.legs.size - 1,
                            destinationName = routeState.toLocation?.shortName ?: routeState.toLocation?.name
                        )
                    }

                    // Next leg preview (if exists)
                    nextLeg?.let {
                        item {
                            NextLegCard(
                                leg = it,
                                isLastLeg = legIndex + 1 == routeState.legs.size - 1,
                                destinationName = routeState.toLocation?.shortName ?: routeState.toLocation?.name,
                                onNextLeg = {
                                    coroutineScope.launch {
                                        if (legIndex < routeState.legs.size - 1) {
                                            pagerState.animateScrollToPage(page + 1)
                                        }
                                    }
                                }
                            )
                        }
                    }

                    // Action buttons
                    item {
                        NavigationActions(
                            currentIndex = legIndex,
                            isComplete = legIndex == routeState.legs.size - 1,
                            hasNextLeg = nextLeg != null,
                            routeSaved = uiState.routeSaved,
                            onPreviousLeg = {
                                coroutineScope.launch {
                                    // Go to previous page (which might be page 0 = back)
                                    pagerState.animateScrollToPage(page - 1)
                                }
                            },
                            onNextLeg = {
                                coroutineScope.launch {
                                    if (legIndex < routeState.legs.size - 1) {
                                        pagerState.animateScrollToPage(page + 1)
                                    }
                                }
                            },
                            onEndNavigation = onEndNavigation,
                            onRefresh = onRefresh,
                            onSaveToFavourites = onSaveToFavourites,
                            onBackToRoutes = onBackToRouteSelection
                        )
                    }
                    }

                    PositionIndicator(lazyListState = lazyListState)
                }
                }
            }
        }
    }
}

@Composable
private fun CurrentLegCard(
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


@Composable
private fun NextLegCard(
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


@Composable
private fun NavigationActions(
    currentIndex: Int,
    isComplete: Boolean,
    hasNextLeg: Boolean,
    routeSaved: Boolean = false,
    onPreviousLeg: () -> Unit,
    onNextLeg: () -> Unit,
    onEndNavigation: () -> Unit,
    onRefresh: () -> Unit,
    onSaveToFavourites: () -> Unit,
    onBackToRoutes: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Show "Routes" on first leg, "Save" on last leg, "Previous" on other legs
        if (currentIndex == 0) {
            // Routes button (on first leg)
            Button(
                onClick = onBackToRoutes,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.filledTonalButtonColors()
            ) {
                Text(
                    text = "Routes",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else if (currentIndex > 0) {
            if (isComplete) {
                // Save to Favourites button (on last leg)
                Button(
                    onClick = onSaveToFavourites,
                    modifier = Modifier.weight(1f),
                    enabled = !routeSaved,
                    colors = if (routeSaved) {
                        ButtonDefaults.buttonColors(containerColor = HslBlue)
                    } else {
                        ButtonDefaults.filledTonalButtonColors()
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (routeSaved) "Saved!" else "Save",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            } else {
                // Previous button (on intermediate legs)
                Button(
                    onClick = onPreviousLeg,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.filledTonalButtonColors()
                ) {
                    Text(
                        text = "Previous",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Next leg or End navigation button
        if (hasNextLeg && !isComplete) {
            Button(
                onClick = onNextLeg,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = HslBlue)
            ) {
                Text(
                    text = "Next",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        if (isComplete) {
            Button(
                onClick = onEndNavigation,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = HslBlue)
            ) {
                Text(
                    text = "End",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun NoActiveRouteScreen() {
    Scaffold(
        timeText = {
            TimeText(timeSource = TimeTextDefaults.timeSource(TimeTextDefaults.timeFormat()))
        }
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.no_active_route),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Start a new route to begin navigation",
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
