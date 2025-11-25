package com.hsl.wear.ui.screens.route

import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.TimeTextDefaults
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material3.*
import com.hsl.wear.data.models.Location
import com.hsl.wear.data.models.AutocompleteResult
import com.hsl.wear.ui.theme.HslBlue
import com.hsl.wear.ui.voice.rememberVoiceInputHelper
import com.hsl.wear.ui.components.MiniRoutePreview
import com.hsl.wear.ui.components.QuickRoutePreview

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
    onClearFromLocation: () -> Unit = {},
    onClearToLocation: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? android.app.Activity ?: return
    val voiceHelper = rememberVoiceInputHelper(activity)

    // Permission launcher for microphone
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            // Handle permission denied case
        }
    }

    // Handle voice recognition results
    val recognizedText by voiceHelper.recognizedText.collectAsState()
    val isListening by voiceHelper.isListening.collectAsState()
    val voiceError by voiceHelper.error.collectAsState()

    // Process voice recognition results
    LaunchedEffect(recognizedText) {
        recognizedText?.let { text ->
            // Simple logic: if no from location is set, use it for from, otherwise use for to
            if (selectedFromLocation == null && fromQuery.isBlank()) {
                onFromQueryChange(text)
            } else if (selectedToLocation == null && toQuery.isBlank()) {
                onToQueryChange(text)
            }
            voiceHelper.clearRecognizedText()
        }
    }

    // Show voice error if any
    LaunchedEffect(voiceError) {
        voiceError?.let {
            // Could show a snackbar or error message
            voiceHelper.clearError()
        }
    }

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
        androidx.wear.compose.foundation.lazy.ScalingLazyColumn(
            modifier = modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            anchorType = androidx.wear.compose.foundation.lazy.ScalingLazyListAnchorType.ItemStart,
            autoCentering = null
        ) {
            item {
                Text(
                    text = "Plan Route",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }

            // From Location Section
            item {
                LocationInputSection(
                    title = "Start",
                    query = fromQuery,
                    selectedLocation = selectedFromLocation,
                    searchResults = fromSearchResults,
                    isLoading = isLoadingFromLocation,
                    isListening = isListening && selectedFromLocation == null,
                    onQueryChange = onFromQueryChange,
                    onUseCurrentLocation = onUseCurrentLocationFrom,
                    onVoiceInput = {
                        // Check microphone permission
                        if (androidx.core.content.ContextCompat.checkSelfPermission(
                                context,
                                android.Manifest.permission.RECORD_AUDIO
                            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                        ) {
                            voiceHelper.startListening("Speak start location")
                        } else {
                            permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    onStopVoiceInput = { voiceHelper.stopListening() },
                    onResultClick = onFromResultClick,
                    onClearLocation = onClearFromLocation,
                    placeholder = "Enter start location"
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Swap Locations Button
            item {
                Button(
                    onClick = {
                        try {
                            onSwapLocations()
                        } catch (e: Exception) {
                            android.util.Log.e("RouteInputScreen", "Error in swap button: ${e.message}", e)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                        .height(32.dp)
                        .semantics { contentDescription = "Swap start and destination" },
                    colors = ButtonDefaults.filledTonalButtonColors(),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Swap",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            // To Location Section
            item {
                LocationInputSection(
                    title = "Destination",
                    query = toQuery,
                    selectedLocation = selectedToLocation,
                    searchResults = toSearchResults,
                    isLoading = isLoadingToLocation,
                    isListening = isListening && selectedFromLocation != null,
                    onQueryChange = onToQueryChange,
                    onUseCurrentLocation = onUseCurrentLocationTo,
                    onVoiceInput = {
                        // Check microphone permission
                        if (androidx.core.content.ContextCompat.checkSelfPermission(
                                context,
                                android.Manifest.permission.RECORD_AUDIO
                            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                        ) {
                            voiceHelper.startListening("Speak destination")
                        } else {
                            permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    onStopVoiceInput = { voiceHelper.stopListening() },
                    onResultClick = onToResultClick,
                    onClearLocation = onClearToLocation,
                    placeholder = "Enter destination"
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
    isListening: Boolean = false,
    onQueryChange: (String) -> Unit,
    onUseCurrentLocation: () -> Unit,
    onVoiceInput: () -> Unit,
    onStopVoiceInput: () -> Unit,
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
                .padding(bottom = 4.dp)
        )

        // Show selected location if available
        selectedLocation?.let { location ->
            Card(
                onClick = {
                    // Clear the selected location to allow editing
                    onClearLocation()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp)
                    .semantics {
                        contentDescription = "Selected ${title.lowercase()} location: ${location.name}. Tap to change"
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

        // Current Location and Voice Input buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Current Location button
            Button(
                onClick = onUseCurrentLocation,
                modifier = Modifier
                    .weight(1f)
                    .height(32.dp)
                    .semantics { contentDescription = "Use current location" },
                enabled = !isLoading && !isListening,
                colors = ButtonDefaults.filledTonalButtonColors(),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (isLoading) {
                        androidx.wear.compose.material.CircularProgressIndicator(
                            modifier = Modifier.size(14.dp)
                        )
                    } else {
                        Text(
                            text = "Location",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Voice Input button
            Button(
                onClick = if (isListening) onStopVoiceInput else onVoiceInput,
                modifier = Modifier
                    .weight(1f)
                    .height(32.dp)
                    .semantics {
                        contentDescription = if (isListening) "Stop voice input" else "Start voice input"
                    },
                colors = if (isListening) {
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                } else {
                    ButtonDefaults.filledTonalButtonColors()
                },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (isListening) "Stop" else "Voice",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isListening) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Input field
        Card(
            onClick = { /* Focus the field */ },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    textStyle = MaterialTheme.typography.labelMedium.copy(
                        textAlign = TextAlign.Start,
                        color = androidx.compose.ui.graphics.Color.White
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = placeholder },
                    decorationBox = { innerTextField ->
                        if (query.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                        innerTextField()
                    }
                )
            }
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