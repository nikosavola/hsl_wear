package com.hsl.wear.network

import com.hsl.wear.data.models.GeocodingResponse
import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GeocodingClientTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var okHttpClient: OkHttpClient
    private lateinit var geocodingClient: GeocodingClient

    @Before
    fun setup() {
        mockkStatic(android.util.Log::class)
        every { android.util.Log.d(any(), any()) } returns 0
        every { android.util.Log.e(any(), any()) } returns 0
        every { android.util.Log.e(any(), any(), any()) } returns 0
        every { android.util.Log.isLoggable(any(), any()) } returns false

        mockWebServer = MockWebServer()
        mockWebServer.start()

        okHttpClient = OkHttpClient.Builder().build()
        val baseUrl = mockWebServer.url("/").toString()
        
        geocodingClient = GeocodingClient(
            client = okHttpClient,
            geocodingEndpoint = baseUrl + "search",
            reverseGeocodingEndpoint = baseUrl + "reverse"
        )
    }

    @After
    fun teardown() {
        mockWebServer.shutdown()
        unmockkStatic(android.util.Log::class)
    }

    @Test
    fun searchLocations_success() = runTest {
        val jsonResponse = """
            {
                "features": [
                    {
                        "properties": {
                            "id": "1",
                            "name": "Kamppi",
                            "layer": "stop",
                            "confidence": 1.0
                        },
                        "geometry": {
                            "coordinates": [24.9, 60.1]
                        }
                    }
                ]
            }
        """
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(jsonResponse))

        val result = geocodingClient.searchLocations("Kamppi")

        assertTrue(result.isSuccess)
        val response = result.getOrNull()
        assertEquals(1, response?.features?.size)
        assertEquals("Kamppi", response?.features?.get(0)?.properties?.name)
    }

    @Test
    fun searchLocations_httpError_fails() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(500).setBody("Server Error"))

        val result = geocodingClient.searchLocations("Kamppi")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("HTTP 500") == true)
    }

    @Test
    fun searchLocations_emptyBody_fails() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(""))

        val result = geocodingClient.searchLocations("Kamppi")

        assertTrue(result.isFailure)
    }

    @Test
    fun searchLocations_malformedJson_fails() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("{ invalid json"))

        val result = geocodingClient.searchLocations("Kamppi")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Failed to parse geocoding response") == true)
    }

    @Test
    fun reverseGeocode_success() = runTest {
        val jsonResponse = """
            {
                "features": [
                    {
                        "properties": {
                            "id": "1",
                            "name": "Kamppi",
                            "layer": "stop",
                            "confidence": 1.0
                        },
                        "geometry": {
                            "coordinates": [24.9, 60.1]
                        }
                    }
                ]
            }
        """
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(jsonResponse))

        val result = geocodingClient.reverseGeocode(60.1, 24.9)

        assertTrue(result.isSuccess)
        val response = result.getOrNull()
        assertEquals(1, response?.features?.size)
        assertEquals("Kamppi", response?.features?.get(0)?.properties?.name)
    }
}
