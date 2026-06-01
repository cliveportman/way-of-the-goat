package co.theportman.way_of_the_goat.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Room database for Way of the Goat (ADR 002).
 *
 * Schema version 1 mirrors the original SQLDelight schema exactly so that an
 * existing on-device database created by the KMP build remains usable. Any
 * future schema change must bump [VERSION] and supply a Migration — never ship
 * a destructive (fallback) migration to release.
 */
@Database(
    entities = [
        UserPreferencesEntity::class,
        DailyServingsEntity::class,
        CategoryServingsEntity::class,
    ],
    version = WayOfTheGoatDatabase.VERSION,
    exportSchema = true,
)
abstract class WayOfTheGoatDatabase : RoomDatabase() {
    abstract fun userPreferencesDao(): UserPreferencesDao
    abstract fun dailyServingsDao(): DailyServingsDao
    abstract fun categoryServingsDao(): CategoryServingsDao

    companion object {
        const val VERSION = 1
        const val DB_NAME = "wayofthegoat.db"

        fun create(context: Context): WayOfTheGoatDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                WayOfTheGoatDatabase::class.java,
                DB_NAME,
            ).build()
    }
}
