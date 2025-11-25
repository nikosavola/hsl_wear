package com.hsl.wear.ui.components.routeinput

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.hsl.wear.data.models.Location
import com.hsl.wear.ui.components.MiniRoutePreview

@Composable
fun QuickRoutePreviewSection(
    fromQuery: String,
    toQuery: String,
    selectedFromLocation: Location?,
    selectedToLocation: Location?,
    onSearchRoutes: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        val canShowPreview = (selectedFromLocation != null || fromQuery.isNotBlank()) &&
                            (selectedToLocation != null || toQuery.isNotBlank())
        val fromLocation = selectedFromLocation?.name ?: if (fromQuery.isNotBlank()) fromQuery else null
        val toLocation = selectedToLocation?.name ?: if (toQuery.isNotBlank()) toQuery else null

        if (canShowPreview && fromLocation != null && toLocation != null) {
            MiniRoutePreview(
                fromLocation = fromLocation,
                toLocation = toLocation,
                estimatedTime = null,
                numberOfTransfers = 0,
                onClick = onSearchRoutes,
                isLoading = false
            )
        }
    }
}