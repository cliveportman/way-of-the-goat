package co.theportman.way_of_the_goat.data.fake

import co.theportman.way_of_the_goat.data.database.CategoryServingsDao
import co.theportman.way_of_the_goat.data.database.CategoryServingsEntity
import co.theportman.way_of_the_goat.data.database.DailyServingsDao
import co.theportman.way_of_the_goat.data.database.DailyServingsEntity
import co.theportman.way_of_the_goat.data.database.TransactionRunner
import co.theportman.way_of_the_goat.data.database.UserPreferencesDao
import co.theportman.way_of_the_goat.data.database.UserPreferencesEntity

/**
 * In-memory fakes mirroring the Room DAO contracts closely enough to exercise
 * [co.theportman.way_of_the_goat.data.repository.ServingsRepository] logic in
 * pure JVM unit tests. They simulate autoincrement ids, the unique(date) and
 * unique(daily_servings_id, category_id) constraints, and the ON DELETE CASCADE
 * from daily_servings to category_servings.
 *
 * Shared state lives in [FakeDb] so the cascade can span the two daily/category fakes.
 */
class FakeDb {
    var prefs: UserPreferencesEntity? = null

    val daily = mutableListOf<DailyServingsEntity>()
    var dailyAutoId = 0L

    val categories = mutableListOf<CategoryServingsEntity>()
    var categoryAutoId = 0L
}

class FakeUserPreferencesDao(private val db: FakeDb) : UserPreferencesDao {
    override suspend fun getPreferences(): UserPreferencesEntity? = db.prefs
    override suspend fun upsert(prefs: UserPreferencesEntity) { db.prefs = prefs.copy(id = 1) }
    override suspend fun updateActiveSuite(suiteId: String, updatedAt: Long) {
        db.prefs = db.prefs?.copy(active_suite_id = suiteId, updated_at = updatedAt)
    }
}

class FakeDailyServingsDao(private val db: FakeDb) : DailyServingsDao {
    override suspend fun getByDate(date: String): DailyServingsEntity? =
        db.daily.firstOrNull { it.date == date }

    override suspend fun getInRange(oldest: String, newest: String): List<DailyServingsEntity> =
        db.daily.filter { it.date in oldest..newest }.sortedByDescending { it.date }

    override suspend fun getLastBeforeDate(date: String): DailyServingsEntity? =
        db.daily.filter { it.date < date }.maxByOrNull { it.date }

    override suspend fun getAllDates(): List<String> =
        db.daily.map { it.date }.distinct().sortedDescending()

    override suspend fun countDaysTracked(): Long = db.daily.size.toLong()

    override suspend fun insert(entity: DailyServingsEntity): Long {
        require(db.daily.none { it.date == entity.date }) { "UNIQUE(date) violated: ${entity.date}" }
        val id = ++db.dailyAutoId
        db.daily.add(entity.copy(id = id))
        return id
    }

    override suspend fun update(entity: DailyServingsEntity) {
        val idx = db.daily.indexOfFirst { it.id == entity.id }
        if (idx >= 0) db.daily[idx] = entity
    }

    override suspend fun deleteByDate(date: String) {
        val removed = db.daily.filter { it.date == date }
        db.daily.removeAll(removed)
        // Simulate ON DELETE CASCADE
        removed.forEach { d -> db.categories.removeAll { it.daily_servings_id == d.id } }
    }
}

class FakeCategoryServingsDao(private val db: FakeDb) : CategoryServingsDao {
    override suspend fun getForDaily(dailyServingsId: Long): List<CategoryServingsEntity> =
        db.categories.filter { it.daily_servings_id == dailyServingsId }

    override suspend fun getForRange(oldest: String, newest: String): List<CategoryServingsEntity> {
        val ids = db.daily.filter { it.date in oldest..newest }.map { it.id }.toSet()
        return db.categories.filter { it.daily_servings_id in ids }
    }

    override suspend fun upsert(entity: CategoryServingsEntity) {
        val idx = db.categories.indexOfFirst {
            it.daily_servings_id == entity.daily_servings_id && it.category_id == entity.category_id
        }
        if (idx >= 0) {
            db.categories[idx] = db.categories[idx].copy(serving_count = entity.serving_count)
        } else {
            db.categories.add(entity.copy(id = ++db.categoryAutoId))
        }
    }

    override suspend fun delete(dailyServingsId: Long, categoryId: String) {
        db.categories.removeAll { it.daily_servings_id == dailyServingsId && it.category_id == categoryId }
    }

    override suspend fun deleteAllForDaily(dailyServingsId: Long) {
        db.categories.removeAll { it.daily_servings_id == dailyServingsId }
    }
}

/** Direct (no-op) transaction runner — fakes are single-threaded in tests. */
class FakeTransactionRunner : TransactionRunner {
    override suspend operator fun <R> invoke(block: suspend () -> R): R = block()
}
