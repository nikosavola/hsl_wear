package com.hsl.wear.ui.components.route

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.*
import com.hsl.wear.R
import com.hsl.wear.ui.theme.HslBlue

@Composable
fun NavigationActions(
    currentIndex: Int,
    isComplete: Boolean,
    hasNextLeg: Boolean,
    routeSaved: Boolean = false,
    onPreviousLeg: () -> Unit,
    onNextLeg: () -> Unit,
    onEndNavigation: () -> Unit,
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
                    text = stringResource(R.string.available_routes),
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
                            text = if (routeSaved) stringResource(R.string.saved) else stringResource(R.string.save_route),
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
                        text = stringResource(R.string.previous),
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
                    text = stringResource(R.string.next),
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
                    text = stringResource(R.string.end),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}