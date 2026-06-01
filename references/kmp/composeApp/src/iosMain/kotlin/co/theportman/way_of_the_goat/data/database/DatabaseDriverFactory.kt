package co.theportman.way_of_the_goat.data.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(
            schema = WayOfTheGoatDatabase.Schema,
            name = "way_of_the_goat.db" // Pass null for in-memory database
        )
    }
}
