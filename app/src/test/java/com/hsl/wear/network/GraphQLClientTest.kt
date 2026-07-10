package com.hsl.wear.network

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GraphQLClientTest {

    @Serializable
    data class TestData(val name: String)

    private lateinit var mockWebServer: MockWebServer
    private lateinit var okHttpClient: OkHttpClient
    private lateinit var graphQLClient: GraphQLClient

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
        graphQLClient = GraphQLClient(client = okHttpClient)
    }

    @After
    fun teardown() {
        mockWebServer.shutdown()
        unmockkStatic(android.util.Log::class)
    }

    @Test
    fun executeQuery_success() = runTest {
        val jsonResponse = """
            {
                "data": {
                    "name": "Test"
                }
            }
        """
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(jsonResponse))

        val baseUrl = mockWebServer.url("/").toString()
        val result = graphQLClient.executeQuery<TestData>(baseUrl, "query { name }")

        assertTrue(result.isSuccess)
        assertEquals("Test", result.getOrNull()?.name)
    }

    @Test
    fun executeQuery_httpError_fails() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(500).setBody("Server Error"))

        val baseUrl = mockWebServer.url("/").toString()
        val result = graphQLClient.executeQuery<TestData>(baseUrl, "query { name }")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("HTTP 500") == true)
    }

    @Test
    fun executeQuery_graphQLError_fails() = runTest {
        val jsonResponse = """
            {
                "errors": [
                    {
                        "message": "Some GraphQL Error"
                    }
                ]
            }
        """
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(jsonResponse))

        val baseUrl = mockWebServer.url("/").toString()
        val result = graphQLClient.executeQuery<TestData>(baseUrl, "query { name }")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Some GraphQL Error") == true)
    }

    @Test
    fun executeQuery_malformedJson_fails() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("{ invalid }"))

        val baseUrl = mockWebServer.url("/").toString()
        val result = graphQLClient.executeQuery<TestData>(baseUrl, "query { name }")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Failed to parse GraphQL response") == true)
    }

    @Test
    fun executeQuery_emptyBody_fails() = runTest {
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(""))

        val baseUrl = mockWebServer.url("/").toString()
        val result = graphQLClient.executeQuery<TestData>(baseUrl, "query { name }")

        assertTrue(result.isFailure)
    }
}
