package com.hsl.wear.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.*

@Composable
fun MiniRoutePreview(
    fromLocation: String,
    toLocation: String,
    estimatedTime: String? = null,
    numberOfTransfers: Int = 0,
    onClick: () -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .width(100.dp)
            .height(40.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        shape = RoundedCornerShape(20.dp),
        contentPadding = PaddingValues(0.dp) // Remove default padding for better control
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Search",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun QuickRoutePreview(
    canShowPreview: Boolean,
    fromLocation: String?,
    toLocation: String?,
    isPlanning: Boolean = false,
    onPlanRoute: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (canShowPreview && fromLocation != null && toLocation != null) {
        MiniRoutePreview(
            fromLocation = fromLocation,
            toLocation = toLocation,
            estimatedTime = if (isPlanning) null else "~15 min", // Would come from actual route planning
            numberOfTransfers = if (isPlanning) 0 else 1, // Would come from actual route planning
            onClick = onPlanRoute,
            isLoading = isPlanning,
            modifier = modifier
        )
    }
}