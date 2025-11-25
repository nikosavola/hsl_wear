package com.hsl.wear.ui.haptics

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.*
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Haptic feedback types specific to HSL Wear app
 */
object HslHaptics {
    // Navigation and selection feedback
    val NAVIGATION_CLICK = HapticFeedbackType.LongPress
    val SELECT_ITEM = HapticFeedbackType.TextHandleMove
    val CONFIRM_ACTION = HapticFeedbackType.Confirm
    val CANCEL_ACTION = HapticFeedbackType.Reject

    // Input and interaction feedback
    val BUTTON_PRESS = HapticFeedbackType.LongPress
    val TOGGLE_SWITCH = HapticFeedbackType.TextHandleMove
    val SLIDER_ADJUST = HapticFeedbackType.TextHandleMove

    // Feedback and notifications
    val SUCCESS = HapticFeedbackType.Confirm
    val ERROR = HapticFeedbackType.Reject
    val WARNING = HapticFeedbackType.LongPress
    val INFO = HapticFeedbackType.TextHandleMove

    // Loading and progress
    val LOADING_START = HapticFeedbackType.TextHandleMove
    val LOADING_COMPLETE = HapticFeedbackType.Confirm
    val PROGRESS_UPDATE = HapticFeedbackType.TextHandleMove

    // Pre-arrival notifications
    val TRANSFER_APPROACHING = HapticFeedbackType.LongPress
    val DESTINATION_APPROACHING = HapticFeedbackType.Confirm
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
    val isPressed by interactionSource.collectIsPressedAsState()

    // Trigger haptic on press
    LaunchedEffect(isPressed) {
        if (isPressed) {
            performHapticFeedback(hapticFeedback, hapticType)
        }
    }

    return interactionSource
}

/**
 * Haptic feedback service for use outside of Compose (e.g., in ViewModels, services)
 */
@Singleton
class HslHapticFeedback @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
        context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? Vibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    fun performLightHaptic() {
        vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    fun performMediumHaptic() {
        vibrator?.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    fun performStrongHaptic() {
        vibrator?.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    fun performConfirmationHaptic() {
        val pattern = longArrayOf(0, 50, 30, 50)
        vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
    }

    fun performSuccessHaptic() {
        val pattern = longArrayOf(0, 100, 50, 100)
        vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
    }

    fun performWarningHaptic() {
        val pattern = longArrayOf(0, 200, 100, 200)
        vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
    }

    fun performTransferHaptic() {
        val pattern = longArrayOf(0, 100, 100, 100, 100)
        vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
    }

    fun performDestinationHaptic() {
        val pattern = longArrayOf(0, 150, 100, 150, 100, 150)
        vibrator?.vibrate(VibrationEffect.createWaveform(pattern, -1))
    }

    // Check if device has vibrator
    fun hasVibrator(): Boolean = vibrator?.hasVibrator() == true

    // Check if device supports amplitude control
    fun hasAmplitudeControl(): Boolean = vibrator?.hasAmplitudeControl() == true
}

/**
 * Simple haptic feedback composable
 */
@Composable
fun HapticFeedbackProvider(
    content: @Composable (androidx.compose.ui.hapticfeedback.HapticFeedback) -> Unit
) {
    val hapticFeedback = rememberHapticFeedback()
    content(hapticFeedback)
}