package com.hsl.wear.utils

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Centralized error messages for consistent user feedback across the app.
 * This class requires a Context to access string resources.
 */
class ErrorMessages(private val context: Context) {

    companion object {
        // Keep the fromException method as a companion for convenience
        /**
         * Gets a user-friendly error message from an exception.
         * @param exception The exception
         * @param defaultMessage Default message if exception type is unknown
         * @return User-friendly error message
         */
        fun fromException(exception: Throwable, defaultMessage: String = "An error occurred"): String {
            return when (exception) {
                is java.net.UnknownHostException -> "Network error occurred. Please check your connection."
                is java.net.SocketTimeoutException -> "Request timed out. Please try again."
                is java.io.IOException -> "Network error occurred. Please check your connection."
                else -> exception.message ?: defaultMessage
            }
        }

        /**
         * Reusable error handling pattern for repository operations.
         * Wraps operations with standard try-catch and logging pattern.
         * @param tag Log tag for debugging
         * @param operation Name of the operation for logging
         * @param block The operation to execute
         * @return Result of the operation, or failure if exception occurs
         */
        suspend inline fun <T> safeExecute(
            tag: String,
            operation: String,
            crossinline block: suspend () -> Result<T>
        ): Result<T> {
            return withContext(Dispatchers.IO) {
                try {
                    android.util.Log.d(tag, "$operation started")
                    val result = block()
                    android.util.Log.d(tag, "$operation completed successfully")
                    result
                } catch (e: Exception) {
                    android.util.Log.e(tag, "$operation failed", e)
                    Result.failure(e)
                }
            }
        }
    }

}
