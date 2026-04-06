package co.theportman.way_of_the_goat.data.auth

import io.ktor.util.encodeBase64

/**
 * Authentication provider using intervals.icu API key.
 * Uses Basic Auth with username "API_KEY" and the API key as password.
 *
 * The API key must be provided at construction time via BuildConfig or
 * an untracked local configuration file. Do not hardcode keys here.
 */
class ApiKeyAuthProvider(
    private val apiKey: String = ""
) : IntervalsAuthProvider {

    override suspend fun getAuthHeader(): String {
        // Basic Auth format: "Basic " + base64("API_KEY:your_api_key")
        val credentials = "API_KEY:$apiKey"
        return "Basic ${credentials.encodeToByteArray().encodeBase64()}"
    }
}
