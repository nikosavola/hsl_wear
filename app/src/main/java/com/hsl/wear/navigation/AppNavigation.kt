package com.hsl.wear.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.navigation.*
import com.hsl.wear.ui.screens.*
import com.hsl.wear.ui.screens.route.RouteInputScreen
import com.hsl.wear.ui.viewmodel.RoutePlanningViewModel
import com.hsl.wear.ui.viewmodel.RouteInputViewModel

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Info : Screen("info")
    object FavouriteRoutes : Screen("favourite_routes")
    object NotificationSettings : Screen("notification_settings")
    object RouteInput : Screen("route_input")
    object RouteSelection : Screen("route_selection")
    object RouteTracking : Screen("route_tracking")
}

@Composable
fun AppNavigation(
    startDestination: String = Screen.Home.route,
    tileDestination: String? = null,
    tileLegIndex: Int? = null,
    navigationKey: Long = 0L
) {
    val navController = rememberSwipeDismissableNavController()
    val routePlanningViewModel: RoutePlanningViewModel = hiltViewModel()
    val sharedRouteInputViewModel: RouteInputViewModel = hiltViewModel()

    // Handle deep link from tile - navigate after composition
    // Use navigationKey to force renavigation even if destination/leg are the same
    LaunchedEffect(navigationKey) {
        if (tileDestination == "route_tracking" && navigationKey > 0) {
            // Pop back to home if we're already in route tracking
            if (navController.currentBackStackEntry?.destination?.route?.startsWith(Screen.RouteTracking.route) == true) {
                navController.popBackStack(Screen.Home.route, false)
            }

            // Navigate to route tracking, keeping Home in back stack
            val route = if (tileLegIndex != null) {
                "${Screen.RouteTracking.route}/$tileLegIndex"
            } else {
                Screen.RouteTracking.route
            }
            navController.navigate(route)
        }
    }

    SwipeDismissableNavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = hiltViewModel(),
                onNewRouteClick = {
                    routePlanningViewModel.reset()
                    sharedRouteInputViewModel.reset()
                    navController.navigate(Screen.RouteInput.route)
                },
                onResumeRouteClick = {
                    navController.navigate(Screen.RouteTracking.route)
                },
                onFavouriteRoutesClick = {
                    navController.navigate(Screen.FavouriteRoutes.route)
                },
                onNotificationSettingsClick = {
                    navController.navigate(Screen.NotificationSettings.route)
                },
                onInfoClick = {
                    navController.navigate(Screen.Info.route)
                }
            )
        }

        composable(Screen.Info.route) {
            InfoScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.NotificationSettings.route) {
            val settingsViewModel = hiltViewModel<com.hsl.wear.ui.viewmodel.NotificationSettingsViewModel>()
            val preferences by settingsViewModel.uiState.collectAsState()

            NotificationSettingsScreen(
                preferences = preferences,
                onPreferencesChanged = settingsViewModel::updatePreferences,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.FavouriteRoutes.route) {
            FavouriteRoutesScreen(
                viewModel = hiltViewModel(),
                onRouteClick = { favoriteRoute ->
                    // Reset and set the from/to locations
                    routePlanningViewModel.reset()
                    routePlanningViewModel.setFromLocation(favoriteRoute.fromLocation)
                    routePlanningViewModel.setToLocation(favoriteRoute.toLocation)
                    // Navigate to route selection
                    navController.navigate(Screen.RouteSelection.route)
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // New simplified route input screen
        composable(Screen.RouteInput.route) {
            // Use the shared RouteInputViewModel instance
            val uiState by sharedRouteInputViewModel.uiState.collectAsState()

            LocationPermissionHandler(
                onPermissionGranted = { /* Permission granted, user can use current location */ },
                onPermissionDenied = { /* Permission denied, handled in ViewModel */ }
            ) {
                RouteInputScreen(
                    fromQuery = uiState.fromQuery,
                    toQuery = uiState.toQuery,
                    selectedFromLocation = uiState.selectedFromLocation,
                    selectedToLocation = uiState.selectedToLocation,
                    fromSearchResults = uiState.fromSearchResults,
                    toSearchResults = uiState.toSearchResults,
                    isLoadingFromLocation = uiState.isLoadingFromLocation,
                    isLoadingToLocation = uiState.isLoadingToLocation,
                    onFromQueryChange = sharedRouteInputViewModel::updateFromQuery,
                    onToQueryChange = sharedRouteInputViewModel::updateToQuery,
                    onUseCurrentLocationFrom = sharedRouteInputViewModel::useCurrentLocationForFrom,
                    onUseCurrentLocationTo = sharedRouteInputViewModel::useCurrentLocationForTo,
                    onFromResultClick = sharedRouteInputViewModel::selectFromLocation,
                    onToResultClick = sharedRouteInputViewModel::selectToLocation,
                    onClearFromLocation = sharedRouteInputViewModel::clearFromLocation,
                    onClearToLocation = sharedRouteInputViewModel::clearToLocation,
                    onSearchRoutes = {
                        val (from, to) = sharedRouteInputViewModel.getLocationsForRoutePlanning()
                        if (from != null && to != null) {
                            routePlanningViewModel.setFromLocation(from)
                            routePlanningViewModel.setToLocation(to)
                            routePlanningViewModel.planRoutes()
                            navController.navigate(Screen.RouteSelection.route)
                        }
                    },
                    onSwapLocations = sharedRouteInputViewModel::swapLocations,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }

        composable(Screen.RouteSelection.route) {
            val uiState by routePlanningViewModel.uiState.collectAsState()

            RouteSelectionScreen(
                viewModel = hiltViewModel(),
                routes = uiState.routes,
                isLoading = uiState.isLoadingRoutes,
                error = uiState.error,
                fromLocation = uiState.selectedFromLocation,
                toLocation = uiState.selectedToLocation,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigationStarted = {
                    // Keep RouteSelection in back stack so we can return to it
                    navController.navigate(Screen.RouteTracking.route)
                }
            )
        }

        composable(
            route = "${Screen.RouteTracking.route}/{legIndex}",
            arguments = listOf(
                androidx.navigation.navArgument("legIndex") {
                    type = androidx.navigation.NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStackEntry ->
            val legIndex = backStackEntry.arguments?.getInt("legIndex") ?: -1
            val viewModel: com.hsl.wear.ui.viewmodel.RouteTrackingViewModel = hiltViewModel()

            // Jump to specific leg if provided
            LaunchedEffect(legIndex) {
                if (legIndex >= 0) {
                    viewModel.jumpToLeg(legIndex)
                }
            }

            RouteTrackingScreen(
                viewModel = viewModel,
                onNavigationEnded = {
                    navController.popBackStack(Screen.Home.route, false)
                },
                onBackToRouteSelection = {
                    // Try to go back to route selection (if in back stack)
                    // If not found (resumed route), go to home instead
                    val poppedToSelection = navController.popBackStack(Screen.RouteSelection.route, false)
                    if (!poppedToSelection) {
                        navController.popBackStack(Screen.Home.route, false)
                    }
                }
            )
        }

        // Also keep the route without leg index for backwards compatibility
        composable(Screen.RouteTracking.route) {
            RouteTrackingScreen(
                viewModel = hiltViewModel(),
                onNavigationEnded = {
                    navController.popBackStack(Screen.Home.route, false)
                },
                onBackToRouteSelection = {
                    val poppedToSelection = navController.popBackStack(Screen.RouteSelection.route, false)
                    if (!poppedToSelection) {
                        navController.popBackStack(Screen.Home.route, false)
                    }
                }
            )
        }
    }
}