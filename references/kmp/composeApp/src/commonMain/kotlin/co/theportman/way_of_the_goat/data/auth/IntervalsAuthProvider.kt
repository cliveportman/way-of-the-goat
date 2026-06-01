package co.theportman.way_of_the_goat.data.auth

/**
 * Abstraction for intervals.icu authentication.
 * Currently supports API key auth, will support OAuth in the future.
 */
interface IntervalsAuthProvider {
    /**
     * Returns the authorization header value for API requests
     */
    suspend fun getAuthHeader(): String
}
