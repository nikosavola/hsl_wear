package com.hsl.wear.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.ScalingLazyListAnchorType
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.TimeTextDefaults
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material3.*
import com.hsl.wear.data.models.Location
import com.hsl.wear.data.models.AutocompleteResult
import com.hsl.wear.ui.components.QuickRoutePreview
import com.hsl.wear.R

@Composable
fun RouteInputScreen(
    fromQuery: String,
    toQuery: String,
    selectedFromLocation: Location? = null,
    selectedToLocation: Location? = null,
    fromSearchResults: List<AutocompleteResult> = emptyList(),
    toSearchResults: List<AutocompleteResult> = emptyList(),
    isLoadingFromLocation: Boolean = false,
    isLoadingToLocation: Boolean = false,
    onFromQueryChange: (String) -> Unit,
    onToQueryChange: (String) -> Unit,
    onUseCurrentLocationFrom: () -> Unit,
    onUseCurrentLocationTo: () -> Unit,
    onFromResultClick: (AutocompleteResult) -> Unit,
    onToResultClick: (AutocompleteResult) -> Unit,
    onSearchRoutes: () -> Unit,
    onSwapLocations: () -> Unit,
    onNavigateBack: () -> Unit,
    onClearFromLocation: () -> Unit,
    onClearToLocation: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Removed auto-navigation - user must manually click to search routes
    val listState = rememberScalingLazyListState()

    Scaffold(
        timeText = {
            TimeText(timeSource = TimeTextDefaults.timeSource(TimeTextDefaults.timeFormat()))
        },
        positionIndicator = {
            PositionIndicator(scalingLazyListState = listState)
        }
    ) {
        ScalingLazyColumn(
            modifier = modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            anchorType = ScalingLazyListAnchorType.ItemStart,
            autoCentering = null
        ) {
            item {
                Text(
                    text = stringResource(R.string.plan_route),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }

            // From Location Section
            item {
                LocationInputSection(
                    title = stringResource(R.string.start_location),
                    query = fromQuery,
                    selectedLocation = selectedFromLocation,
                    searchResults = fromSearchResults,
                    isLoading = isLoadingFromLocation,
                    onQueryChange = onFromQueryChange,
                    onUseCurrentLocation = onUseCurrentLocationFrom,
                    onResultClick = onFromResultClick,
                    onClearLocation = onClearFromLocation,
                    placeholder = stringResource(R.string.enter_start_location)
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Swap Locations Button
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                shape = CircleShape
                            )
                            .clickable {
                                try {
                                    onSwapLocations()
                                } catch (e: Exception) {
                                    Log.e("RouteInputScreen", "Error in swap button: ${e.message}", e)
                                }
                            }
                            .semantics { contentDescription = "Swap start and destination" },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "⇅",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            // To Location Section
            item {
                LocationInputSection(
                    title = stringResource(R.string.destination_location),
                    query = toQuery,
                    selectedLocation = selectedToLocation,
                    searchResults = toSearchResults,
                    isLoading = isLoadingToLocation,
                    onQueryChange = onToQueryChange,
                    onUseCurrentLocation = onUseCurrentLocationTo,
                    onResultClick = onToResultClick,
                    onClearLocation = onClearToLocation,
                    placeholder = stringResource(R.string.enter_destination)
                )
            }

            // Quick Route Preview
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    QuickRoutePreview(
                        canShowPreview = (selectedFromLocation != null || fromQuery.isNotBlank()) &&
                                        (selectedToLocation != null || toQuery.isNotBlank()),
                        fromLocation = selectedFromLocation?.name ?: if (fromQuery.isNotBlank()) fromQuery else null,
                        toLocation = selectedToLocation?.name ?: if (toQuery.isNotBlank()) toQuery else null,
                        onPlanRoute = onSearchRoutes
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun LocationInputSection(
    title: String,
    query: String,
    selectedLocation: Location?,
    searchResults: List<AutocompleteResult>,
    isLoading: Boolean,
    onQueryChange: (String) -> Unit,
    onUseCurrentLocation: () -> Unit,
    onResultClick: (AutocompleteResult) -> Unit,
    onClearLocation: () -> Unit = {},
    placeholder: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        // Show selected location if available
        selectedLocation?.let { location ->
            val contentDesc = stringResource(R.string.selected_location, title.lowercase(), location.name)
            Card(
                onClick = {
                    // Clear the selected location to allow editing
                    onClearLocation()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp)
                    .semantics {
                        contentDescription = contentDesc
                    },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = location.name,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Start,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "✕",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
            return@Column // Return early if location is selected
        }

        // Current Location button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp)
                )
                .clickable(enabled = !isLoading) { onUseCurrentLocation() }
                .padding(horizontal = 18.dp, vertical = 14.dp)
                .semantics { contentDescription = "Use current location" },
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp)
                )
            } else {
                Text(
                    text = stringResource(R.string.use_current_location),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Input field
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp)
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(28.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(28.dp)
                )
                .padding(horizontal = 18.dp, vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                textStyle = MaterialTheme.typography.labelMedium.copy(
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = placeholder },
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (query.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                textAlign = TextAlign.Center
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }

        // Add spacing before search results or no results message
        if (searchResults.isNotEmpty() || (query.isNotEmpty() && !isLoading && searchResults.isEmpty())) {
            Spacer(modifier = Modifier.height(12.dp))
        }

        // No results message
        if (query.isNotEmpty() && !isLoading && searchResults.isEmpty()) {
            Text(
                text = stringResource(R.string.no_results),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                textAlign = TextAlign.Center
            )
        }

        // Search Results
        searchResults.take(3).forEach { result ->
            Card(
                onClick = { onResultClick(result) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 1.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = result.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    result.lines?.take(2)?.forEach { line ->
                        Text(
                            text = line,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = MaterialTheme.typography.labelSmall.fontSize * 0.8f
                            ),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}