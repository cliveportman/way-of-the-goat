package co.theportman.way_of_the_goat.data.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entities mirroring the original SQLDelight schema (see ADR 002).
 *
 * Schema is intentionally identical to the KMP build's `WayOfTheGoatDatabase.sq`
 * so existing on-device data remains compatible:
 *   - user_preferences: single-row settings (active suite)
 *   - daily_servings:   one row per date, tied to the suite active when recorded
 *   - category_servings: normalised per-category counts, FK -> daily_servings (cascade)
 */

@Entity(tableName = "user_preferences")
data class UserPreferencesEntity(
    @PrimaryKey val id: Long = 1,
    val active_suite_id: String,
    val created_at: Long,
    val updated_at: Long,
)

@Entity(
    tableName = "daily_servings",
    indices = [
        Index(value = ["date"], unique = true),
        Index(value = ["suite_id"]),
    ],
)
data class DailyServingsEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String,        // ISO 8601: YYYY-MM-DD
    val suite_id: String,
    val created_at: Long,    // epoch millis
    val updated_at: Long,    // epoch millis
)

@Entity(
    tableName = "category_servings",
    foreignKeys = [
        ForeignKey(
            entity = DailyServingsEntity::class,
            parentColumns = ["id"],
            childColumns = ["daily_servings_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["daily_servings_id"]),
        Index(value = ["daily_servings_id", "category_id"], unique = true),
    ],
)
data class CategoryServingsEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val daily_servings_id: Long,
    val category_id: String,
    val serving_count: Int = 0,
)
