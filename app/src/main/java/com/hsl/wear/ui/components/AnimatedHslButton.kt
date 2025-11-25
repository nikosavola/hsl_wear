package com.hsl.wear.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.*
import com.hsl.wear.ui.theme.HslBlue
import androidx.compose.animation.core.*

@Composable
fun AnimatedHslButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    buttonType: HslButtonType = HslButtonType.PRIMARY
) {
    val hapticFeedback = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Animate scale for press feedback
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween<Float>(200, easing = EaseOutCubic),
        label = "ButtonScale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .height(52.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(
                color = when {
                    !enabled -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                    isLoading -> MaterialTheme.colorScheme.secondaryContainer
                    isPressed -> when (buttonType) {
                        HslButtonType.PRIMARY -> HslBlue.copy(alpha = 0.8f)
                        HslButtonType.SECONDARY -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f)
                        HslButtonType.TERTIARY -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.8f)
                    }
                    else -> when (buttonType) {
                        HslButtonType.PRIMARY -> HslBlue
                        HslButtonType.SECONDARY -> MaterialTheme.colorScheme.secondary
                        HslButtonType.TERTIARY -> MaterialTheme.colorScheme.tertiary
                    }
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    if (enabled && !isLoading) {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        onClick()
                    }
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.1.sp
                    ),
                    color = when (buttonType) {
                        HslButtonType.PRIMARY -> MaterialTheme.colorScheme.onPrimary
                        else -> MaterialTheme.colorScheme.onSecondary
                    },
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.1.sp
                ),
                color = when (buttonType) {
                    HslButtonType.PRIMARY -> MaterialTheme.colorScheme.onPrimary
                    HslButtonType.SECONDARY -> MaterialTheme.colorScheme.onSecondary
                    HslButtonType.TERTIARY -> MaterialTheme.colorScheme.onTertiary
                }.copy(alpha = if (enabled) 1f else 0.6f),
                textAlign = TextAlign.Center
            )
        }
    }
}

enum class HslButtonType {
    PRIMARY,
    SECONDARY,
    TERTIARY
}