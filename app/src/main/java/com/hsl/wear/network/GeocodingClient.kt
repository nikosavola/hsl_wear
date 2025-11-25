package com.hsl.wear.network

import com.hsl.wear.BuildConfig
import com.hsl.wear.data.models.GeocodingResponse
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
        private const val GEOCODING_ENDPOINT = "https://api.digitransit.fi/geocoding/v1/search"
        private const val REVERSE_GEOCODING_ENDPOINT = "https://api.digitransit.fi/geocoding/v1/reverse"
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
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
        return try {
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
