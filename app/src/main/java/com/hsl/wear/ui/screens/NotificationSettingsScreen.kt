package com.hsl.wear.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.*
import com.hsl.wear.data.models.NotificationPreferences

@Composable
fun NotificationSettingsScreen(
    preferences: NotificationPreferences,
    onPreferencesChanged: (NotificationPreferences) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentPreferences by remember { mutableStateOf(preferences) }
    val listState = rememberScalingLazyListState()

    Scaffold(
        positionIndicator = {
            PositionIndicator(scalingLazyListState = listState)
        }
    ) {
        ScalingLazyColumn(
            modifier = modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(
                horizontal = 16.dp,
                vertical = 8.dp
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
        item {
            Text(
                text = "Notifications",
                style = MaterialTheme.typography.title2,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Master Toggle
        item {
            val toggleIcon = if (currentPreferences.preArrivalEnabled) {
                Icons.Default.Check
            } else {
                Icons.Default.Close
            }

            Chip(
                icon = { Icon(toggleIcon, contentDescription = null) },
                onClick = {
                    currentPreferences = currentPreferences.copy(preArrivalEnabled = !currentPreferences.preArrivalEnabled)
                    onPreferencesChanged(currentPreferences)
                },
                label = { Text("Pre-arrival alerts") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Only show other settings if notifications are enabled
        if (currentPreferences.preArrivalEnabled) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Timing Settings
            item {
                Text(
                    text = "Alert Before",
                    style = MaterialTheme.typography.caption1,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                // Simple timing selector using ToggleChip
                Chip(
                    onClick = {
                        val newMinutes = if (currentPreferences.advanceMinutes == 2) 3 else if (currentPreferences.advanceMinutes == 3) 5 else 2
                        currentPreferences = currentPreferences.copy(advanceMinutes = newMinutes)
                        onPreferencesChanged(currentPreferences)
                    },
                    label = { Text("${currentPreferences.advanceMinutes} min") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Notification Types
            item {
                Text(
                    text = "Alert For",
                    style = MaterialTheme.typography.caption1,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                val transferIcon = if (currentPreferences.transferNotificationsEnabled) {
                    Icons.Default.Check
                } else {
                    Icons.Default.Close
                }

                Chip(
                    icon = { Icon(transferIcon, contentDescription = null) },
                    onClick = {
                        currentPreferences = currentPreferences.copy(
                            transferNotificationsEnabled = !currentPreferences.transferNotificationsEnabled
                        )
                        onPreferencesChanged(currentPreferences)
                    },
                    label = { Text("Transfers") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                val destinationIcon = if (currentPreferences.finalDestinationNotificationsEnabled) {
                    Icons.Default.Check
                } else {
                    Icons.Default.Close
                }

                Chip(
                    icon = { Icon(destinationIcon, contentDescription = null) },
                    onClick = {
                        currentPreferences = currentPreferences.copy(
                            finalDestinationNotificationsEnabled = !currentPreferences.finalDestinationNotificationsEnabled
                        )
                        onPreferencesChanged(currentPreferences)
                    },
                    label = { Text("Final Destination") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Walking Notifications
            item {
                Text(
                    text = "Walking Phases",
                    style = MaterialTheme.typography.caption1,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                val walkingIcon = if (currentPreferences.walkingNotificationsEnabled) {
                    Icons.Default.Check
                } else {
                    Icons.Default.Close
                }

                Chip(
                    icon = { Icon(walkingIcon, contentDescription = null) },
                    onClick = {
                        currentPreferences = currentPreferences.copy(
                            walkingNotificationsEnabled = !currentPreferences.walkingNotificationsEnabled
                        )
                        onPreferencesChanged(currentPreferences)
                    },
                    label = { Text("Walking Alerts") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        }
    }
}