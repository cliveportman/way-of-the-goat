package co.theportman.way_of_the_goat.data.remote

import co.theportman.way_of_the_goat.data.auth.IntervalsAuthProvider
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

/**
 * Factory for creating configured HttpClient instances for intervals.icu API
 */
object HttpClientFactory {

    fun create(authProvider: IntervalsAuthProvider): HttpClient {
        return HttpClient {
            // JSON serialization
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true // Ignore fields we don't map
                    prettyPrint = true
                    isLenient = true
                })
            }

            // Logging for debugging
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.INFO
            }

            // Default request configuration - adds auth header to all requests
            defaultRequest {
                header("Authorization", runBlocking { authProvider.getAuthHeader() })
            }
        }
    }
}
