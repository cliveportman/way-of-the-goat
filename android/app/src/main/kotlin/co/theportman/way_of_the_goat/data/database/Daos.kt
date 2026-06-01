package co.theportman.way_of_the_goat.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

/**
 * DAOs are declared as interfaces so the repository can be unit-tested against
 * lightweight in-memory fakes without Robolectric or a real device DB. Room
 * generates the production implementations from these interfaces via KSP.
 */

@Dao
interface UserPreferencesDao {
    @Query("SELECT * FROM user_preferences WHERE id = 1")
    suspend fun getPreferences(): UserPreferencesEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(prefs: UserPreferencesEntity)

    @Query("UPDATE user_preferences SET active_suite_id = :suiteId, updated_at = :updatedAt WHERE id = 1")
    suspend fun updateActiveSuite(suiteId: String, updatedAt: Long)
}

@Dao
interface DailyServingsDao {
    @Query("SELECT * FROM daily_servings WHERE date = :date")
    suspend fun getByDate(date: String): DailyServingsEntity?

    @Query("SELECT * FROM daily_servings WHERE date >= :oldest AND date <= :newest ORDER BY date DESC")
    suspend fun getInRange(oldest: String, newest: String): List<DailyServingsEntity>

    @Query("SELECT * FROM daily_servings WHERE date < :date ORDER BY date DESC LIMIT 1")
    suspend fun getLastBeforeDate(date: String): DailyServingsEntity?

    @Query("SELECT DISTINCT date FROM daily_servings ORDER BY date DESC")
    suspend fun getAllDates(): List<String>

    @Query("SELECT COUNT(*) FROM daily_servings")
    suspend fun countDaysTracked(): Long

    @Insert
    suspend fun insert(entity: DailyServingsEntity): Long

    @Update
    suspend fun update(entity: DailyServingsEntity)

    @Query("DELETE FROM daily_servings WHERE date = :date")
    suspend fun deleteByDate(date: String)
}

@Dao
interface CategoryServingsDao {
    @Query("SELECT * FROM category_servings WHERE daily_servings_id = :dailyServingsId")
    suspend fun getForDaily(dailyServingsId: Long): List<CategoryServingsEntity>

    @Query(
        "SELECT cs.* FROM category_servings cs " +
            "INNER JOIN daily_servings ds ON ds.id = cs.daily_servings_id " +
            "WHERE ds.date >= :oldest AND ds.date <= :newest",
    )
    suspend fun getForRange(oldest: String, newest: String): List<CategoryServingsEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: CategoryServingsEntity)

    @Query("DELETE FROM category_servings WHERE daily_servings_id = :dailyServingsId AND category_id = :categoryId")
    suspend fun delete(dailyServingsId: Long, categoryId: String)

    @Query("DELETE FROM category_servings WHERE daily_servings_id = :dailyServingsId")
    suspend fun deleteAllForDaily(dailyServingsId: Long)
}
