package co.theportman.way_of_the_goat.data.database

import app.cash.sqldelight.db.SqlDriver

/**
 * Factory for creating platform-specific SQLDelight drivers.
 * Implemented in androidMain and iosMain source sets.
 */
expect class DatabaseDriverFactory {
    fun createDriver(): SqlDriver
}
