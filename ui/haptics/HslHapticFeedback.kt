package com.hsl.wear.ui.haptics

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.*
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * Haptic feedback types specific to HSL Wear app
 */
object HslHaptics {
    // Navigation and selection feedback
    const val NAVIGATION_CLICK = HapticFeedbackType.LongPress
    const val SELECT_ITEM = HapticFeedbackType.TextHandleMove
    const val CONFIRM_ACTION = HapticFeedbackType.Confirm
    const val CANCEL_ACTION = HapticFeedbackType.Reject

    // Input and interaction feedback
    const val BUTTON_PRESS = HapticFeedbackType.LongPress
    const val TOGGLE_SWITCH = HapticFeedbackType.ClockTick
    const val SLIDER_ADJUST = HapticFeedbackType.TextHandleMove

    // Feedback and notifications
    const val SUCCESS = HapticFeedbackType.Confirm
    const val ERROR = HapticFeedbackType.Reject
    const val WARNING = HapticFeedbackType.LongPress
    const val INFO = HapticFeedbackType.ClockTick

    // Loading and progress
    const val LOADING_START = HapticFeedbackType.TextHandleMove
    const val LOADING_COMPLETE = HapticFeedbackType.Confirm
    const val PROGRESS_UPDATE = HapticFeedbackType.ClockTick
}

/**
 * Provides haptic feedback with safety checks
 */
@Composable
fun rememberHapticFeedback() = LocalHapticFeedback.current

/**
 * Perform haptic feedback safely with null checks
 */
fun performHapticFeedback(
    hapticFeedback: androidx.compose.ui.hapticfeedback.HapticFeedback?,
    hapticType: HapticFeedbackType
) {
    hapticFeedback?.performHapticFeedback(hapticType)
}

/**
 * Create interaction source with haptic feedback
 */
@Composable
fun rememberHapticInteractionSource(
    hapticType: HapticFeedbackType = HslHaptics.BUTTON_PRESS
): MutableInteractionSource {
    val hapticFeedback = rememberHapticFeedback()
    val interactionSource = remember { MutableInteractionSource() }

    // Trigger haptic on press
    LaunchedEffect(interactionSource.collectIsPressedAsState().value) {
        if (interactionSource.collectIsPressedAsState().value) {
            performHapticFeedback(hapticFeedback, hapticType)
        }
    }

    return interactionSource
}

/**
 * Haptic feedback for route planning actions
 */
object RoutePlanningHaptics {
    @Composable
    fun onRouteSelected() = rememberHapticFeedback().also {
        performHapticFeedback(it, HslHaptics.SELECT_ITEM)
    }

    @Composable
    fun onLocationPicked() = rememberHapticFeedback().also {
        performHapticFeedback(it, HslHaptics.CONFIRM_ACTION)
    }

    @Composable
    fun onSearchComplete() = rememberHapticFeedback().also {
        performHapticFeedback(it, HslHaptics.SUCCESS)
    }

    @Composable
    fun onError() = rememberHapticFeedback().also {
        performHapticFeedback(it, HslHaptics.ERROR)
    }
}

/**
 * Haptic feedback for navigation
 */
object NavigationHaptics {
    @Composable
    fun onNavigate() = rememberHapticFeedback().also {
        performHapticFeedback(it, HslHaptics.NAVIGATION_CLICK)
    }

    @Composable
    fun onSwipeBack() = rememberHapticFeedback().also {
        performHapticFeedback(it, HslHaptics.NAVIGATION_CLICK)
    }

    @Composable
    fun onTabSwitch() = rememberHapticFeedback().also {
        performHapticFeedback(it, HslHaptics.TOGGLE_SWITCH)
    }
}

/**
 * Haptic feedback for input interactions
 */
object InputHaptics {
    @Composable
    fun onTextFocus() = rememberHapticFeedback().also {
        performHapticFeedback(it, HslHaptics.CLOCK_TICK)
    }

    @Composable
    fun onVoiceInputStart() = rememberHapticFeedback().also {
        performHapticFeedback(it, HslHaptics.CONFIRM_ACTION)
    }

    @Composable
    fun onLocationSuggested() = rememberHapticFeedback().also {
        performHapticFeedback(it, HslHaptics.INFO)
    }
}

/**
 * Haptic feedback for loading states
 */
object LoadingHaptics {
    @Composable
    fun onStartLoading() = rememberHapticFeedback().also {
        performHapticFeedback(it, HslHaptics.LOADING_START)
    }

    @Composable
    fun onComplete() = rememberHapticFeedback().also {
        performHapticFeedback(it, HslHaptics.LOADING_COMPLETE)
    }

    @Composable
    fun onProgress() = rememberHapticFeedback().also {
        performHapticFeedback(it, HslHaptics.PROGRESS_UPDATE)
    }
}