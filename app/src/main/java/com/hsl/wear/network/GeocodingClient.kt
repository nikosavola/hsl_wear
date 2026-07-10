package com.hsl.wear.network

import com.hsl.wear.BuildConfig
import com.hsl.wear.data.models.GeocodingResponse
import com.hsl.wear.utils.constants.NetworkConstants
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeocodingClient @Inject constructor(
    private val client: OkHttpClient,
    private val json: Json
) {

    companion object {
        private const val GEOCODING_ENDPOINT = NetworkConstants.GEOCODING_ENDPOINT
        private const val REVERSE_GEOCODING_ENDPOINT = NetworkConstants.REVERSE_GEOCODING_ENDPOINT

            }

  suspend fun searchLocations(query: String): Result<GeocodingResponse> {
        return searchLocationsWithBoundaries(query)
    }

    /**
     * Search locations using the geocoding API
     * @param query Search query text
     */
    suspend fun searchLocationsWithBoundaries(query: String): Result<GeocodingResponse> {
        return try {
            val url = GEOCODING_ENDPOINT + "?text=" + java.net.URLEncoder.encode(query, "UTF-8")

            val request = Request.Builder()
                .url(url)
                .get()
                .addHeader("Accept", "application/json")
                .apply {
                    if (BuildConfig.HSL_API_KEY.isNotEmpty()) {
                        addHeader("digitransit-subscription-key", BuildConfig.HSL_API_KEY)
                    }
                }
                .build()

            val response = client.newCall(request).execute()
            response.use {
                if (!response.isSuccessful) {
                    val errorBody = response.body?.string() ?: "No error body"
                    android.util.Log.e("GeocodingClient", "HTTP ${response.code}: ${response.message}")
                    Result.failure(IOException("HTTP ${response.code}: ${response.message}"))
                } else {
                    val responseBody = response.body?.string()
                        ?: return Result.failure(IOException("Empty response body"))

                    try {
                        val geocodingResponse = json.decodeFromString<GeocodingResponse>(responseBody)
                        Result.success(geocodingResponse)
                    } catch (e: Exception) {
                        android.util.Log.e("GeocodingClient", "Failed to parse geocoding response: ${e.message}")
                        Result.failure(IOException("Failed to parse geocoding response: ${e.message}", e))
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("GeocodingClient", "Geocoding request failed: ${e.message}")
            Result.failure(IOException("Geocoding request failed: ${e.message}", e))
        }
    }

    suspend fun reverseGeocode(lat: Double, lon: Double): Result<GeocodingResponse> {
        return try {
            val url = "${REVERSE_GEOCODING_ENDPOINT}?point.lat=${lat}&point.lon=${lon}"

            val request = Request.Builder()
                .url(url)
                .get()
                .addHeader("Accept", "application/json")
                .apply {
                    if (BuildConfig.HSL_API_KEY.isNotEmpty()) {
                        addHeader("digitransit-subscription-key", BuildConfig.HSL_API_KEY)
                    }
                }
                .build()

            val response = client.newCall(request).execute()
            response.use {
                if (!response.isSuccessful) {
                    val errorBody = response.body?.string() ?: "No error body"
                    android.util.Log.e("GeocodingClient", "HTTP ${response.code}: ${response.message}")
                    Result.failure(IOException("HTTP ${response.code}: ${response.message}"))
                } else {
                    val responseBody = response.body?.string()
                        ?: return Result.failure(IOException("Empty response body"))

                    try {
                        val geocodingResponse = json.decodeFromString<GeocodingResponse>(responseBody)
                        Result.success(geocodingResponse)
                    } catch (e: Exception) {
                        android.util.Log.e("GeocodingClient", "Failed to parse geocoding response: ${e.message}")
                        Result.failure(IOException("Failed to parse geocoding response: ${e.message}", e))
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("GeocodingClient", "Reverse geocoding request failed: ${e.message}")
            Result.failure(IOException("Reverse geocoding request failed: ${e.message}", e))
        }
    }
}
