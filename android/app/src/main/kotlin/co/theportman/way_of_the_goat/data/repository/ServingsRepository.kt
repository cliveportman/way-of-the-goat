package co.theportman.way_of_the_goat.data.repository

import co.theportman.way_of_the_goat.data.database.CategoryServingsDao
import co.theportman.way_of_the_goat.data.database.CategoryServingsEntity
import co.theportman.way_of_the_goat.data.database.DailyServingsDao
import co.theportman.way_of_the_goat.data.database.DailyServingsEntity
import co.theportman.way_of_the_goat.data.database.TransactionRunner
import co.theportman.way_of_the_goat.data.database.UserPreferencesDao
import co.theportman.way_of_the_goat.data.database.UserPreferencesEntity
import co.theportman.way_of_the_goat.data.scoring.DailyScoreResult
import co.theportman.way_of_the_goat.data.scoring.SuiteDefinitions
import co.theportman.way_of_the_goat.data.scoring.model.CategoryId
import co.theportman.way_of_the_goat.data.scoring.model.DailyServings
import co.theportman.way_of_the_goat.data.scoring.model.ScoringSuite
import co.theportman.way_of_the_goat.data.scoring.model.SuiteId
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate

/**
 * Repository for managing food servings data, backed by Room (ADR 002).
 *
 * Ported from the SQLDelight implementation in the KMP build; the public API is
 * unchanged so callers (e.g. [co.theportman.way_of_the_goat.data.cache.ServingsDataManager])
 * are unaffected by the persistence swap. Depends on DAO interfaces and a
 * [TransactionRunner] seam so it can be unit-tested without a real database.
 *
 * Every public method returns `Result<T>` and never throws (except for
 * structured-concurrency [CancellationException], which is rethrown).
 */
class ServingsRepository(
    private val preferencesDao: UserPreferencesDao,
    private val dailyServingsDao: DailyServingsDao,
    private val categoryServingsDao: CategoryServingsDao,
    private val transactionRunner: TransactionRunner,
    private val now: () -> Long = { Clock.System.now().toEpochMilliseconds() },
) {
    private val _activeSuite = MutableStateFlow(SuiteDefinitions.defaultSuite)
    val activeSuite: StateFlow<ScoringSuite> = _activeSuite.asStateFlow()

    /** Initialize repository and load user preferences (creating defaults on first run). */
    suspend fun initialize(): Result<Unit> = runResult {
        val prefs = preferencesDao.getPreferences()
        if (prefs != null) {
            val suite = SuiteDefinitions.getSuiteById(SuiteId(prefs.active_suite_id))
                ?: SuiteDefinitions.defaultSuite
            _activeSuite.value = suite
        } else {
            val ts = now()
            preferencesDao.upsert(
                UserPreferencesEntity(
                    id = 1,
                    active_suite_id = SuiteDefinitions.defaultSuite.id.value,
                    created_at = ts,
                    updated_at = ts,
                ),
            )
        }
    }

    /** Switch the user's default scoring suite (used for TODAY when no data exists). */
    suspend fun setActiveSuite(suiteId: SuiteId): Result<Unit> = runResult {
        val suite = SuiteDefinitions.getSuiteById(suiteId)
            ?: throw IllegalArgumentException("Unknown suite: ${suiteId.value}")
        preferencesDao.updateActiveSuite(suiteId.value, now())
        _activeSuite.value = suite
    }

    /** Most recent servings record strictly before [date] (for the "last used profile" hint). */
    suspend fun getLastServingsBeforeDate(date: LocalDate): Result<DailyServings?> = runResult {
        val row = dailyServingsDao.getLastBeforeDate(date.toString()) ?: return@runResult null
        row.toDailyServings(categoryServingsDao.getForDaily(row.id))
    }

    /** Servings record for a specific [date], or null if none exists. */
    suspend fun getServingsForDate(date: LocalDate): Result<DailyServings?> = runResult {
        val row = dailyServingsDao.getByDate(date.toString()) ?: return@runResult null
        row.toDailyServings(categoryServingsDao.getForDaily(row.id))
    }

    /** Servings records within an inclusive date range, newest first (for the history screen). */
    suspend fun getServingsForRange(
        oldest: LocalDate,
        newest: LocalDate,
    ): Result<List<DailyServings>> = runResult {
        val rows = dailyServingsDao.getInRange(oldest.toString(), newest.toString())
        if (rows.isEmpty()) return@runResult emptyList()

        val categoriesByDaily = categoryServingsDao
            .getForRange(oldest.toString(), newest.toString())
            .groupBy { it.daily_servings_id }

        rows.map { row -> row.toDailyServings(categoriesByDaily[row.id].orEmpty()) }
            .sortedByDescending { it.date }
    }

    /** Insert or update the full servings record for a date (atomic). */
    suspend fun saveServings(dailyServings: DailyServings): Result<Unit> = runResult {
        transactionRunner {
            val ts = now()
            val existing = dailyServingsDao.getByDate(dailyServings.date.toString())

            val dailyId: Long = if (existing == null) {
                dailyServingsDao.insert(
                    DailyServingsEntity(
                        date = dailyServings.date.toString(),
                        suite_id = dailyServings.suiteId.value,
                        created_at = ts,
                        updated_at = ts,
                    ),
                )
            } else {
                dailyServingsDao.update(existing.copy(suite_id = dailyServings.suiteId.value, updated_at = ts))
                categoryServingsDao.deleteAllForDaily(existing.id)
                existing.id
            }

            dailyServings.servings.forEach { (categoryId, count) ->
                if (count > 0) {
                    categoryServingsDao.upsert(
                        CategoryServingsEntity(
                            daily_servings_id = dailyId,
                            category_id = categoryId.value,
                            serving_count = count,
                        ),
                    )
                }
            }
        }
    }

    /** Set a single category's serving count for a date, creating the day record if needed (atomic). */
    suspend fun updateCategoryServings(
        date: LocalDate,
        categoryId: CategoryId,
        servingCount: Int,
    ): Result<Unit> = runResult {
        transactionRunner {
            val ts = now()
            val existing = dailyServingsDao.getByDate(date.toString())

            val dailyId: Long = if (existing == null) {
                dailyServingsDao.insert(
                    DailyServingsEntity(
                        date = date.toString(),
                        suite_id = _activeSuite.value.id.value,
                        created_at = ts,
                        updated_at = ts,
                    ),
                )
            } else {
                dailyServingsDao.update(existing.copy(updated_at = ts))
                existing.id
            }

            if (servingCount > 0) {
                categoryServingsDao.upsert(
                    CategoryServingsEntity(
                        daily_servings_id = dailyId,
                        category_id = categoryId.value,
                        serving_count = servingCount,
                    ),
                )
            } else {
                categoryServingsDao.delete(dailyId, categoryId.value)
            }
        }
    }

    /** Delete the servings record for a date (cascades to category rows). */
    suspend fun deleteServingsForDate(date: LocalDate): Result<Unit> = runResult {
        dailyServingsDao.deleteByDate(date.toString())
    }

    /** Compute the score for a date using the suite recorded against it; null if no data or unknown suite. */
    suspend fun calculateScoreForDate(date: LocalDate): Result<DailyScoreResult?> =
        getServingsForDate(date).map { dailyServings ->
            if (dailyServings == null) return@map null
            val suite = SuiteDefinitions.getSuiteById(dailyServings.suiteId) ?: return@map null
            DailyScoreResult(
                date = date,
                score = dailyServings.calculateScore(suite) ?: 0,
                suiteId = suite.id,
                suiteName = suite.name,
                maxPossibleScore = suite.maxPossibleDailyScore,
            )
        }

    /** Total number of days that have a record. */
    suspend fun getDaysTrackedCount(): Result<Long> = runResult { dailyServingsDao.countDaysTracked() }

    /** All dates that have records, newest first. */
    suspend fun getAllDatesWithRecords(): Result<List<LocalDate>> = runResult {
        dailyServingsDao.getAllDates().map { LocalDate.parse(it) }
    }

    private fun DailyServingsEntity.toDailyServings(
        categories: List<CategoryServingsEntity>,
    ): DailyServings = DailyServings(
        id = id,
        date = LocalDate.parse(date),
        suiteId = SuiteId(suite_id),
        servings = categories.associate { CategoryId(it.category_id) to it.serving_count },
        createdAt = created_at,
        updatedAt = updated_at,
    )
}

/**
 * Run [block], wrapping the outcome in [Result]. Rethrows [CancellationException]
 * so coroutine cancellation is never swallowed.
 */
private inline fun <T> runResult(block: () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
