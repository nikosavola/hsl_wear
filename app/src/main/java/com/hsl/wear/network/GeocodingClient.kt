package com.hsl.wear.network

import com.hsl.wear.BuildConfig
import com.hsl.wear.data.models.GeocodingResponse
import com.hsl.wear.utils.constants.NetworkConstants
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeocodingClient @Inject constructor() {

    companion object {
        private const val GEOCODING_ENDPOINT = NetworkConstants.GEOCODING_ENDPOINT
        private const val REVERSE_GEOCODING_ENDPOINT = NetworkConstants.REVERSE_GEOCODING_ENDPOINT

        // Helsinki metropolitan area boundaries (Helsinki, Espoo, Vantaa, Kauniainen)
        // Source: GPS coordinates for accurate metropolitan area coverage
        private const val MIN_LAT = 60.16952   // Southern boundary (Helsinki)
        private const val MAX_LAT = 60.29414   // Northern boundary (Vantaa)
        private const val MIN_LON = 24.6522    // Western boundary (Espoo)
        private const val MAX_LON = 25.04099   // Eastern boundary (Vantaa)
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)  // Will be replaced with NetworkConstants in next step
        .readTimeout(30, TimeUnit.SECONDS)      // Will be replaced with NetworkConstants in next step
        .writeTimeout(30, TimeUnit.SECONDS)      // Will be replaced with NetworkConstants in next step
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = if (android.util.Log.isLoggable("HSLNetwork", android.util.Log.DEBUG)) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        })
        .build()

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = false
    }

  suspend fun searchLocations(query: String): Result<GeocodingResponse> {
        return searchLocationsWithBoundaries(query, MIN_LAT, MAX_LAT, MIN_LON, MAX_LON)
    }

    /**
     * Search locations with custom boundary rectangle
     * @param query Search query text
     * @param minLat Minimum latitude (southern boundary)
     * @param maxLat Maximum latitude (northern boundary)
     * @param minLon Minimum longitude (western boundary)
     * @param maxLon Maximum longitude (eastern boundary)
     */
    suspend fun searchLocationsWithBoundaries(
        query: String,
        minLat: Double = MIN_LAT,
        maxLat: Double = MAX_LAT,
        minLon: Double = MIN_LON,
        maxLon: Double = MAX_LON
    ): Result<GeocodingResponse> {
        return try {
            // Add boundary rectangle to limit results to specified area
            val url = GEOCODING_ENDPOINT + "?text=" + java.net.URLEncoder.encode(query, "UTF-8")
            android.util.Log.d("GeocodingClient", "Searching for: $query, URL: $url")
            android.util.Log.d("GeocodingClient", "API Key present: ${BuildConfig.HSL_API_KEY.isNotEmpty()}")

            val request = Request.Builder()
                .url(url)
                .get()
                .addHeader("Accept", "application/json")
                .apply {
                    // Add API key header if available
                    if (BuildConfig.HSL_API_KEY.isNotEmpty()) {
                        addHeader("digitransit-subscription-key", BuildConfig.HSL_API_KEY)
                    }
                }
                .build()

            val response = client.newCall(request).execute()
            response.use {
                android.util.Log.d("GeocodingClient", "Response code: ${response.code}")
                if (!response.isSuccessful) {
                    val errorBody = response.body?.string() ?: "No error body"
                    android.util.Log.e("GeocodingClient", "HTTP ${response.code}: ${response.message}, Body: $errorBody")
                    Result.failure(IOException("HTTP ${response.code}: ${response.message}"))
                } else {
                    val responseBody = response.body?.string()
                        ?: return Result.failure(IOException("Empty response body"))

                    android.util.Log.d("GeocodingClient", "Response body: ${responseBody.take(500)}")

                    try {
                        val geocodingResponse = json.decodeFromString<GeocodingResponse>(responseBody)
                        android.util.Log.d("GeocodingClient", "Parsed ${geocodingResponse.features.size} features")
                        Result.success(geocodingResponse)
                    } catch (e: Exception) {
                        android.util.Log.e("GeocodingClient", "Failed to parse response", e)
                        Result.failure(IOException("Failed to parse geocoding response: ${e.message}", e))
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("GeocodingClient", "Request failed", e)
            Result.failure(IOException("Geocoding request failed: ${e.message}", e))
        }
    }

    suspend fun reverseGeocode(lat: Double, lon: Double): Result<GeocodingResponse> {
        return try {
            val url = "${REVERSE_GEOCODING_ENDPOINT}?point.lat=${lat}&point.lon=${lon}"
            android.util.Log.d("GeocodingClient", "Reverse geocoding: $lat, $lon, URL: $url")
            android.util.Log.d("GeocodingClient", "API Key present: ${BuildConfig.HSL_API_KEY.isNotEmpty()}")

            val request = Request.Builder()
                .url(url)
                .get()
                .addHeader("Accept", "application/json")
                .apply {
                    // Add API key header if available
                    if (BuildConfig.HSL_API_KEY.isNotEmpty()) {
                        addHeader("digitransit-subscription-key", BuildConfig.HSL_API_KEY)
                    }
                }
                .build()

            val response = client.newCall(request).execute()
            response.use {
                android.util.Log.d("GeocodingClient", "Response code: ${response.code}")
                if (!response.isSuccessful) {
                    val errorBody = response.body?.string() ?: "No error body"
                    android.util.Log.e("GeocodingClient", "HTTP ${response.code}: ${response.message}, Body: $errorBody")
                    Result.failure(IOException("HTTP ${response.code}: ${response.message}"))
                } else {
                    val responseBody = response.body?.string()
                        ?: return Result.failure(IOException("Empty response body"))

                    android.util.Log.d("GeocodingClient", "Response body: ${responseBody.take(500)}")

                    try {
                        val geocodingResponse = json.decodeFromString<GeocodingResponse>(responseBody)
                        android.util.Log.d("GeocodingClient", "Parsed ${geocodingResponse.features.size} features")
                        Result.success(geocodingResponse)
                    } catch (e: Exception) {
                        android.util.Log.e("GeocodingClient", "Failed to parse response", e)
                        Result.failure(IOException("Failed to parse geocoding response: ${e.message}", e))
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("GeocodingClient", "Request failed", e)
            Result.failure(IOException("Reverse geocoding request failed: ${e.message}", e))
        }
    }
}
