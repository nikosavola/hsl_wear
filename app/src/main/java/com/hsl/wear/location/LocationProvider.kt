package com.hsl.wear.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class LocationProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    /**
     * Check if location permissions are granted
     */
    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Get the current location using FusedLocationProviderClient
     * Returns null if permissions are not granted or location is unavailable
     *
     * Strategy: Force fresh location first, then try lastLocation as fallback
     */
    suspend fun getCurrentLocation(): Location? = suspendCancellableCoroutine { continuation ->
        android.util.Log.d("LocationProvider", "getCurrentLocation called")

        if (!hasLocationPermission()) {
            android.util.Log.e("LocationProvider", "Location permission not granted")
            continuation.resume(null)
            return@suspendCancellableCoroutine
        }

        android.util.Log.d("LocationProvider", "Location permission granted, requesting fresh location...")

        // Strategy: Try to get fresh current location first (picks up Extended Controls changes)
        tryGetCurrentLocation(continuation)
    }

    /**
     * Try to get a fresh current location
     */
    @Suppress("MissingPermission")
    private fun tryGetCurrentLocation(
        continuation: kotlinx.coroutines.CancellableContinuation<Location?>
    ) {
        android.util.Log.d("LocationProvider", "Trying to get fresh current location...")
        try {
            val cancellationTokenSource = CancellationTokenSource()

            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).addOnSuccessListener { location ->
                if (location != null) {
                    android.util.Log.d("LocationProvider", "Fresh location received: ${location.latitude}, ${location.longitude}")
                    continuation.resume(location)
                } else {
                    android.util.Log.w("LocationProvider", "Fresh location is null, trying lastLocation")
                    // Try lastLocation before LocationManager
                    tryLastLocation(continuation)
                }
            }.addOnFailureListener { exception ->
                android.util.Log.e("LocationProvider", "Fresh location failed: ${exception.message}", exception)
                tryLastLocation(continuation)
            }
        } catch (e: SecurityException) {
            android.util.Log.e("LocationProvider", "SecurityException getting fresh location: ${e.message}", e)
            continuation.resume(null)
        }
    }

    /**
     * Try to get last known location
     */
    @Suppress("MissingPermission")
    private fun tryLastLocation(
        continuation: kotlinx.coroutines.CancellableContinuation<Location?>
    ) {
        android.util.Log.d("LocationProvider", "Trying last known location...")
        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        android.util.Log.d("LocationProvider", "Last location: ${location.latitude}, ${location.longitude}")
                        continuation.resume(location)
                    } else {
                        android.util.Log.w("LocationProvider", "Last location is null, trying LocationManager")
                        tryLocationManager(continuation)
                    }
                }
                .addOnFailureListener { exception ->
                    android.util.Log.e("LocationProvider", "Last location failed: ${exception.message}", exception)
                    tryLocationManager(continuation)
                }
        } catch (e: SecurityException) {
            android.util.Log.e("LocationProvider", "SecurityException getting last location: ${e.message}", e)
            continuation.resume(null)
        }
    }

    /**
     * Fallback to last known location if current location fails
     */
    @Suppress("MissingPermission")
    private fun tryGetLastKnownLocation(
        continuation: kotlinx.coroutines.CancellableContinuation<Location?>
    ) {
        android.util.Log.d("LocationProvider", "Trying last known location as fallback")
        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        android.util.Log.d("LocationProvider", "Last known location: ${location.latitude}, ${location.longitude}")
                        continuation.resume(location)
                    } else {
                        android.util.Log.w("LocationProvider", "Last known location is also null, trying LocationManager")
                        // Final fallback: try LocationManager (works better with emulator mocked locations)
                        tryLocationManager(continuation)
                    }
                }
                .addOnFailureListener { exception ->
                    android.util.Log.e("LocationProvider", "Last known location failed: ${exception.message}", exception)
                    // Try LocationManager as final fallback
                    tryLocationManager(continuation)
                }
        } catch (e: SecurityException) {
            android.util.Log.e("LocationProvider", "SecurityException getting last location: ${e.message}", e)
            continuation.resume(null)
        }
    }

    /**
     * Final fallback: Use LocationManager directly (works better with emulator)
     */
    @Suppress("MissingPermission")
    private fun tryLocationManager(
        continuation: kotlinx.coroutines.CancellableContinuation<Location?>
    ) {
        android.util.Log.d("LocationProvider", "Trying LocationManager as final fallback")
        try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

            // Try GPS provider first, then network provider
            val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)

            for (provider in providers) {
                if (locationManager.isProviderEnabled(provider)) {
                    val location = locationManager.getLastKnownLocation(provider)
                    if (location != null) {
                        android.util.Log.d("LocationProvider", "LocationManager ($provider) returned: ${location.latitude}, ${location.longitude}")
                        continuation.resume(location)
                        return
                    }
                }
            }

            android.util.Log.w("LocationProvider", "LocationManager also returned null")
            continuation.resume(null)
        } catch (e: Exception) {
            android.util.Log.e("LocationProvider", "LocationManager failed: ${e.message}", e)
            continuation.resume(null)
        }
    }

    /**
     * Request location updates (useful for continuous tracking)
     */
    @Suppress("MissingPermission")
    suspend fun requestLocationUpdates(
        onLocationReceived: (Location) -> Unit
    ): LocationCallback? {
        if (!hasLocationPermission()) {
            return null
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            10000 // 10 seconds
        ).build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    onLocationReceived(location)
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
            return locationCallback
        } catch (e: SecurityException) {
            return null
        }
    }

    /**
     * Stop location updates
     */
    fun removeLocationUpdates(locationCallback: LocationCallback) {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}
