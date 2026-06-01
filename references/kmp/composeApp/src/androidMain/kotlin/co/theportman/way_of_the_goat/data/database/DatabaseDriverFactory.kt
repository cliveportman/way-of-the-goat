package co.theportman.way_of_the_goat.data.database

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            schema = WayOfTheGoatDatabase.Schema,
            context = context,
            name = "way_of_the_goat.db" // Pass null for in-memory database
        )
    }
}
