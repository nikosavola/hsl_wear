package com.hsl.wear.network

import com.hsl.wear.BuildConfig
import com.hsl.wear.data.models.GraphQLRequest
import com.hsl.wear.data.models.GraphQLResponse
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GraphQLClient @Inject constructor(
    private val client: OkHttpClient,
    @PublishedApi internal val json: Json
) {

    suspend inline fun <reified T> executeQuery(
        endpoint: String,
        query: String,
        variables: Map<String, String> = emptyMap()
    ): Result<T> {
        val rawResult = executeRaw(endpoint, query, variables)
        return rawResult.fold(
            onSuccess = { responseBody ->
                try {
                    val graphQLResponse = json.decodeFromString<GraphQLResponse<T>>(responseBody)
                    graphQLResponse.data?.let { data ->
                        android.util.Log.d("GraphQLClient", "Successfully parsed GraphQL response")
                        Result.success(data)
                    } ?: graphQLResponse.errors?.firstOrNull()?.let { error ->
                        android.util.Log.e("GraphQLClient", "GraphQL Error: ${error.message}")
                        Result.failure(IOException("GraphQL Error: ${error.message}"))
                    } ?: Result.failure(IOException("Unknown GraphQL response format"))
                } catch (e: Exception) {
                    android.util.Log.e("GraphQLClient", "Failed to parse response", e)
                    Result.failure(IOException("Failed to parse GraphQL response: ${e.message}", e))
                }
            },
            onFailure = {
                Result.failure(it)
            }
        )
    }

    @PublishedApi
    internal fun executeRaw(
        endpoint: String,
        query: String,
        variables: Map<String, String>
    ): Result<String> {
        return try {
            android.util.Log.d("GraphQLClient", "Executing query to endpoint: $endpoint")
            val request = GraphQLRequest(query, variables)
            val requestBody = json.encodeToString(
                GraphQLRequest.serializer(),
                request
            ).toRequestBody("application/json".toMediaType())

            val httpRequest = Request.Builder()
                .url(endpoint)
                .post(requestBody)
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .apply {
                    // Add API key header if available
                    if (BuildConfig.HSL_API_KEY.isNotEmpty()) {
                        addHeader("digitransit-subscription-key", BuildConfig.HSL_API_KEY)
                    }
                }
                .build()

            val response = client.newCall(httpRequest).execute()
            response.use {
                android.util.Log.d("GraphQLClient", "Response code: ${response.code}")
                if (!response.isSuccessful) {
                    val errorBody = response.body?.string() ?: "No error body"
                    android.util.Log.e("GraphQLClient", "HTTP ${response.code}: ${response.message}, Body: $errorBody")
                    Result.failure(IOException("HTTP ${response.code}: ${response.message}"))
                } else {
                    val responseBody = response.body?.string()
                        ?: return Result.failure(IOException("Empty response body"))

                    android.util.Log.d("GraphQLClient", "Response body: ${responseBody.take(500)}")
                    Result.success(responseBody)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("GraphQLClient", "Network request failed", e)
            Result.failure(IOException("Network request failed: ${e.message}", e))
        }
    }
}
