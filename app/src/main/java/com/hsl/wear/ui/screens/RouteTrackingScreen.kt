package com.hsl.wear.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import android.content.res.Resources
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
import com.hsl.wear.ui.components.route.CurrentLegCard
import com.hsl.wear.ui.components.route.NextLegCard
import com.hsl.wear.ui.components.route.NavigationActions
import com.hsl.wear.ui.models.RouteTrackingUiState
import com.hsl.wear.ui.theme.HslBlue
import com.hsl.wear.ui.viewmodel.RouteTrackingViewModel
import com.hsl.wear.utils.TimeFormatter

// Helper functions for time calculations
private fun calculateStatusMinutes(leg: Leg, currentTime: Long): Int =
    ((TimeFormatter.parseIsoTime(leg.realtimeTimeIso ?: leg.scheduledTimeIso) - currentTime) / (1000 * 60)).toInt()

private fun calculateArrivalTimeInfo(leg: Leg, currentTime: Long): Pair<Long, Int> {
    val startTime = TimeFormatter.parseIsoTime(leg.realtimeTimeIso ?: leg.scheduledTimeIso)
    val arrivalTime = startTime + (leg.duration * 1000)
    val arrivalMinutes = ((arrivalTime - currentTime) / (1000 * 60)).toInt()
    return Pair(arrivalTime, arrivalMinutes)
}

private fun getStatusDisplayText(statusMinutes: Int, isWalking: Boolean, resources: Resources): String = when {
    isWalking && statusMinutes > 0 -> resources.getString(R.string.start_walking_in, statusMinutes)
    isWalking -> resources.getString(R.string.on_route)
    statusMinutes > 0 -> resources.getString(R.string.departing_in, statusMinutes)
    statusMinutes == 0 -> resources.getString(R.string.departs_now)
    else -> resources.getString(R.string.departed)
}

private fun getArrivalDisplayText(arrivalMinutes: Int, resources: Resources): String = when {
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
fun RouteTrackingScreen(
    viewModel: RouteTrackingViewModel,
    onNavigationEnded: () -> Unit,
    onBackToRouteSelection: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    // Lifecycle-aware: Start updates when screen is visible, stop when not visible
    DisposableEffect(Unit) {
        viewModel.startRealtimeUpdates()
        onDispose {
            viewModel.stopRealtimeUpdates()
        }
    }

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
            onRefreshCurrentLeg = viewModel::refreshCurrentLeg,
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
    onRefreshCurrentLeg: () -> Unit,
    onBackToRouteSelection: () -> Unit,
    onSaveToFavourites: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Pager configuration: Page 0 = Back page, Pages 1-N = Legs 0-(N-1)
    val totalPages: Int = routeState.legs.size + 1
    val initialLegPage: Int = routeState.currentIndex + 1 // Offset by 1 for back page

    val routePagerState: PagerState = rememberPagerState(
        initialPage = initialLegPage,
        pageCount = { totalPages }
    )
    val navigationScope: CoroutineScope = rememberCoroutineScope()

    // Ensure we start at the correct page on initial load
    LaunchedEffect(Unit) {
        val expectedLegPage: Int = routeState.currentIndex + 1
        if (routePagerState.currentPage != expectedLegPage) {
            routePagerState.scrollToPage(expectedLegPage)
        }
    }

    // Sync pager with route state changes (but don't interfere with back navigation on page 0)
    LaunchedEffect(routeState.currentIndex) {
        val expectedLegPage: Int = routeState.currentIndex + 1
        val isBackNavigationPage: Boolean = routePagerState.currentPage == 0
        if (routePagerState.currentPage != expectedLegPage && !isBackNavigationPage) {
            routePagerState.animateScrollToPage(expectedLegPage)
        }
    }

    // Handle page 0 (back navigation page)
    LaunchedEffect(routePagerState.currentPage) {
        if (routePagerState.currentPage == 0) {
            onBackToRouteSelection()
        }
    }

    Scaffold(
        timeText = {
            TimeText(timeSource = TimeTextDefaults.timeSource(TimeTextDefaults.timeFormat()))
        }
    ) {
        HorizontalPager(
            state = routePagerState,
            modifier = modifier.fillMaxSize(),
            userScrollEnabled = true
        ) { currentPageIndex ->
            // Page 0 = Back indicator, Pages 1+ = Legs
            if (currentPageIndex == 0) {
                // Back navigation page - show a simple indicator
                BackNavigationPage()
            } else {
                val currentLegIndex: Int = currentPageIndex - 1
                val currentLeg: Leg? = routeState.legs.getOrNull(currentLegIndex)
                val nextLeg: Leg? = routeState.legs.getOrNull(currentLegIndex + 1)
                val legListState: LazyListState = rememberLazyListState()

                if (currentLeg != null) {
                Box(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = legListState,
                        contentPadding = PaddingValues(start = 8.dp, end = 8.dp, top = 0.dp, bottom = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                    // Current leg with hero-style countdown
                    item {
                        CurrentLegCard(
                            leg = currentLeg,
                            currentTime = uiState.currentTime,
                            isActive = true,
                            isLastLeg = currentLegIndex == routeState.legs.size - 1,
                            destinationName = routeState.toLocation?.shortName ?: routeState.toLocation?.name,
                            isRefreshing = uiState.isRefreshing,
                            onClick = onRefreshCurrentLeg
                        )
                    }

                    // Next leg preview (if exists)
                    nextLeg?.let { nextLegData ->
                        item {
                            NextLegCard(
                                leg = nextLegData,
                                isLastLeg = currentLegIndex + 1 == routeState.legs.size - 1,
                                destinationName = routeState.toLocation?.shortName ?: routeState.toLocation?.name,
                                onNextLeg = {
                                    navigationScope.launch {
                                        if (currentLegIndex < routeState.legs.size - 1) {
                                            routePagerState.animateScrollToPage(currentPageIndex + 1)
                                        }
                                    }
                                }
                            )
                        }
                    }

                    // Action buttons
                    item {
                        NavigationActions(
                            currentIndex = currentLegIndex,
                            isComplete = currentLegIndex == routeState.legs.size - 1,
                            hasNextLeg = nextLeg != null,
                            routeSaved = uiState.routeSaved,
                            onPreviousLeg = {
                                navigationScope.launch {
                                    // Go to previous page (which might be page 0 = back)
                                    routePagerState.animateScrollToPage(currentPageIndex - 1)
                                }
                            },
                            onNextLeg = {
                                navigationScope.launch {
                                    if (currentLegIndex < routeState.legs.size - 1) {
                                        routePagerState.animateScrollToPage(currentPageIndex + 1)
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

                    PositionIndicator(lazyListState = legListState)
                }
                }
            }
        }
    }
}

@Composable
private fun BackNavigationPage() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.back_navigation),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
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
    val context = LocalContext.current

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
            TransportModeSection(leg)
            Spacer(modifier = Modifier.height(16.dp))

            // Explicit departure countdown
            val statusMinutes = calculateStatusMinutes(leg, currentTime)
            Text(
                text = getStatusDisplayText(statusMinutes, leg.isWalking, context.resources),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = if (leg.hasRealtimeData) HslBlue else MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Station name
            Text(
                text = when {
                    leg.isWalking && isLastLeg && destinationName != null -> stringResource(R.string.walk_to_destination, destinationName)
                    leg.isWalking -> stringResource(R.string.walk_to_destination, leg.toStopName)
                    else -> stringResource(R.string.from_location, leg.fromStopName)
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
                TransitInfoSection(leg, arrivalMinutes)
            }
        }
    }
}

@Composable
private fun WalkingInfoSection(leg: Leg, arrivalMinutes: Int, resources: Resources) {
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
private fun TransitInfoSection(leg: Leg, arrivalMinutes: Int) {
    val numStops = leg.intermediateStops.size + 1
    Text(
        text = "${stringResource(if (numStops == 1) R.string.stop_count_one else R.string.stop_count_many, numStops)} | ${leg.duration / 60} min",
        style = MaterialTheme.typography.labelMedium,
        color = Color.LightGray
    )

    // Terminal station
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = stringResource(R.string.to_location, leg.toStopName),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center,
        maxLines = 3
    )

    // Arrival time for transit (after "To:" line)
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = getArrivalDisplayText(arrivalMinutes, LocalContext.current.resources),
        style = MaterialTheme.typography.labelMedium,
        color = if (leg.hasRealtimeData) HslBlue else Color.LightGray
    )
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
                    text = stringResource(R.string.start_new_route),
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
