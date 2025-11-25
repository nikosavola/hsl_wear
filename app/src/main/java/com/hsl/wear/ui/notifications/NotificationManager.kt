package com.hsl.wear.ui.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.hsl.wear.R
import com.hsl.wear.data.models.Leg
import com.hsl.wear.data.models.NotificationPreferences
import com.hsl.wear.data.models.VibrationIntensity
import com.hsl.wear.ui.haptics.HslHapticFeedback
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val hapticFeedback: HslHapticFeedback
) {

    companion object {
        const val TRANSFER_NOTIFICATION_CHANNEL_ID = "hsl_transfer_notifications"
        const val DESTINATION_NOTIFICATION_CHANNEL_ID = "hsl_destination_notifications"
        const val CHANNEL_NAME = "HSL Transit Notifications"
    }

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        // Create transfer notification channel
        val transferChannel = NotificationChannel(
            TRANSFER_NOTIFICATION_CHANNEL_ID,
            "Transfer Notifications",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifications for upcoming transit transfers"
            enableVibration(true)
            setShowBadge(false)
        }

        // Create destination notification channel
        val destinationChannel = NotificationChannel(
            DESTINATION_NOTIFICATION_CHANNEL_ID,
            "Destination Notifications",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifications for approaching final destination"
            enableVibration(true)
            setShowBadge(false)
        }

        notificationManager.createNotificationChannels(listOf(transferChannel, destinationChannel))
    }

    fun showTransferNotification(leg: Leg, legIndex: Int, preferences: NotificationPreferences) {
        coroutineScope.launch {
            try {
                // Create notification content
                val title = "Transfer Approaching"
                val content = "${leg.transportDisplayName} to ${leg.toStopName}"

                // Create notification
                val notification = NotificationCompat.Builder(context, TRANSFER_NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .setLocalOnly(true) // Show only on this device
                    .setOngoing(false)
                    .build()

                // Show notification
                notificationManager.notify(legIndex, notification)

                // Trigger haptic feedback
                triggerHapticFeedback(preferences, NotificationType.TRANSFER)

            } catch (e: Exception) {
                // Handle notification display error
            }
        }
    }

    fun showDestinationNotification(leg: Leg, preferences: NotificationPreferences) {
        coroutineScope.launch {
            try {
                // Create notification content
                val title = "Destination Approaching"
                val content = "Arriving at ${leg.toStopName} soon"

                // Create notification
                val notification = NotificationCompat.Builder(context, DESTINATION_NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
                    .setLocalOnly(true) // Show only on this device
                    .setOngoing(false)
                    .build()

                // Show notification
                notificationManager.notify(9999, notification) // Use fixed ID for destination

                // Trigger haptic feedback
                triggerHapticFeedback(preferences, NotificationType.DESTINATION)

            } catch (e: Exception) {
                // Handle notification display error
            }
        }
    }

    private fun triggerHapticFeedback(preferences: NotificationPreferences, type: NotificationType) {
        if (preferences.vibrationOnly) {
            // Only vibration, no on-screen notification
            when (type) {
                NotificationType.TRANSFER -> performTransferVibration(preferences.vibrationIntensity)
                NotificationType.DESTINATION -> performDestinationVibration(preferences.vibrationIntensity)
            }
        } else {
            // Both vibration and on-screen notification (handled above)
            when (type) {
                NotificationType.TRANSFER -> performTransferVibration(preferences.vibrationIntensity)
                NotificationType.DESTINATION -> performDestinationVibration(preferences.vibrationIntensity)
            }
        }
    }

    private fun performTransferVibration(intensity: VibrationIntensity) {
        when (intensity) {
            VibrationIntensity.LIGHT -> hapticFeedback.performLightHaptic()
            VibrationIntensity.MEDIUM -> hapticFeedback.performMediumHaptic()
            VibrationIntensity.STRONG -> hapticFeedback.performStrongHaptic()
        }
    }

    private fun performDestinationVibration(intensity: VibrationIntensity) {
        when (intensity) {
            VibrationIntensity.LIGHT -> hapticFeedback.performConfirmationHaptic()
            VibrationIntensity.MEDIUM -> hapticFeedback.performSuccessHaptic()
            VibrationIntensity.STRONG -> hapticFeedback.performWarningHaptic()
        }
    }

    fun cancelTransferNotification(legIndex: Int) {
        try {
            notificationManager.cancel(legIndex)
        } catch (e: Exception) {
            // Handle cancellation error
        }
    }

    fun cancelDestinationNotification() {
        try {
            notificationManager.cancel(9999)
        } catch (e: Exception) {
            // Handle cancellation error
        }
    }

    fun cancelAllNotifications() {
        try {
            notificationManager.cancelAll()
        } catch (e: Exception) {
            // Handle cancellation error
        }
    }

    private enum class NotificationType {
        TRANSFER, DESTINATION
    }
}