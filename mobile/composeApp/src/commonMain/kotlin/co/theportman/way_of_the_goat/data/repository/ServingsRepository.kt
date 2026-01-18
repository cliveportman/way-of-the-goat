package co.theportman.way_of_the_goat.data.repository

import co.theportman.way_of_the_goat.data.database.WayOfTheGoatDatabase
import co.theportman.way_of_the_goat.data.scoring.DailyScoreResult
import co.theportman.way_of_the_goat.data.scoring.SuiteDefinitions
import co.theportman.way_of_the_goat.data.scoring.model.CategoryId
import co.theportman.way_of_the_goat.data.scoring.model.DailyServings
import co.theportman.way_of_the_goat.data.scoring.model.SuiteId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate

/**
 * Repository for managing food servings data.
 * Follows the existing Repository pattern in the codebase.
 */
class ServingsRepository(
    private val database: WayOfTheGoatDatabase
) {
    private val queries = database.wayOfTheGoatDatabaseQueries

    /**
     * Currently active suite (from user preferences)
     */
    private val _activeSuite = MutableStateFlow(SuiteDefinitions.defaultSuite)
    val activeSuite: StateFlow<co.theportman.way_of_the_goat.data.scoring.model.ScoringSuite> =
        _activeSuite.asStateFlow()

    /**
     * Initialize repository and load user preferences.
     */
    suspend fun initialize(): Result<Unit> {
        return try {
            val prefs = queries.getPreferences().executeAsOneOrNull()
            if (prefs != null) {
                val suiteId = SuiteId(prefs.active_suite_id)
                val suite = SuiteDefinitions.getSuiteById(suiteId) ?: SuiteDefinitions.defaultSuite
                _activeSuite.value = suite
            } else {
                // First run - create default preferences
                val now = Clock.System.now().toEpochMilliseconds()
                queries.upsertPreferences(
                    active_suite_id = SuiteDefinitions.defaultSuite.id.value,
                    created_at = now,
                    updated_at = now
                )
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Switch to a different scoring suite.
     * Note: This only updates the user's default preference.
     * Use recordProfileChange() to also record in profile history.
     */
    suspend fun setActiveSuite(suiteId: SuiteId): Result<Unit> {
        return try {
            val suite = SuiteDefinitions.getSuiteById(suiteId)
                ?: return Result.failure(IllegalArgumentException("Unknown suite: ${suiteId.value}"))

            val now = Clock.System.now().toEpochMilliseconds()
            queries.updateActiveSuite(
                active_suite_id = suiteId.value,
                updated_at = now
            )

            _activeSuite.value = suite
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Record a profile change in the history table.
     * This tracks when a profile became active for a specific date,
     * allowing empty days to display the correct historical profile.
     */
    suspend fun recordProfileChange(suiteId: SuiteId, effectiveDate: LocalDate): Result<Unit> {
        return try {
            val now = Clock.System.now().toEpochMilliseconds()
            queries.insertProfileHistory(
                suite_id = suiteId.value,
                effective_date = effectiveDate.toString(),
                created_at = now
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get the profile that was active for a specific date.
     * Queries the profile_history table for the most recent change
     * on or before the given date.
     * Returns null if no history exists (fallback to current activeSuite).
     */
    suspend fun getProfileForDate(date: LocalDate): Result<SuiteId?> {
        return try {
            val suiteIdStr = queries.getProfileForDate(date.toString()).executeAsOneOrNull()
            val suiteId = suiteIdStr?.let { SuiteId(it) }
            Result.success(suiteId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get servings record for a specific date.
     */
    suspend fun getServingsForDate(date: LocalDate): Result<DailyServings?> {
        return try {
            val rows = queries.getDailyServingsForDate(date.toString()).executeAsList()

            if (rows.isEmpty()) {
                Result.success(null)
            } else {
                val firstRow = rows.first()
                val servingsMap = rows
                    .filter { it.category_id != null && it.serving_count != null }
                    .associate { CategoryId(it.category_id!!) to it.serving_count!!.toInt() }

                Result.success(
                    DailyServings(
                        id = firstRow.id,
                        date = LocalDate.parse(firstRow.date),
                        suiteId = SuiteId(firstRow.suite_id),
                        servings = servingsMap,
                        createdAt = firstRow.created_at,
                        updatedAt = firstRow.updated_at
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get servings records for a date range (for Progress screen).
     */
    suspend fun getServingsForRange(
        oldest: LocalDate,
        newest: LocalDate
    ): Result<List<DailyServings>> {
        return try {
            val rows = queries.getDailyServingsForRange(
                oldest.toString(),
                newest.toString()
            ).executeAsList()

            // Group rows by daily_servings id
            val groupedRows = rows.groupBy { it.id }

            val dailyServingsList = groupedRows.map { (_, rowList) ->
                val firstRow = rowList.first()
                val servingsMap = rowList
                    .filter { it.category_id != null && it.serving_count != null }
                    .associate { CategoryId(it.category_id!!) to it.serving_count!!.toInt() }

                DailyServings(
                    id = firstRow.id,
                    date = LocalDate.parse(firstRow.date),
                    suiteId = SuiteId(firstRow.suite_id),
                    servings = servingsMap,
                    createdAt = firstRow.created_at,
                    updatedAt = firstRow.updated_at
                )
            }.sortedByDescending { it.date }

            Result.success(dailyServingsList)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Save or update servings for a date.
     */
    suspend fun saveServings(dailyServings: DailyServings): Result<Unit> {
        return try {
            database.transaction {
                val now = Clock.System.now().toEpochMilliseconds()

                // Check if record exists for this date
                val existing = queries.getDailyServingsRecordForDate(
                    dailyServings.date.toString()
                ).executeAsOneOrNull()

                val dailyServingsId: Long

                if (existing == null) {
                    // Insert new record
                    queries.insertDailyServings(
                        date = dailyServings.date.toString(),
                        suite_id = dailyServings.suiteId.value,
                        created_at = now,
                        updated_at = now
                    )
                    dailyServingsId = queries.lastInsertRowId().executeAsOne()
                } else {
                    // Update existing record
                    dailyServingsId = existing.id
                    queries.updateDailyServings(
                        suite_id = dailyServings.suiteId.value,
                        updated_at = now,
                        id = dailyServingsId
                    )

                    // Clear existing category servings
                    queries.deleteAllCategoryServingsForDaily(dailyServingsId)
                }

                // Insert all category servings
                dailyServings.servings.forEach { (categoryId, count) ->
                    if (count > 0) {
                        queries.insertCategoryServings(
                            daily_servings_id = dailyServingsId,
                            category_id = categoryId.value,
                            serving_count = count.toLong()
                        )
                    }
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update a single category's serving count for a date.
     * More efficient than saving the entire record.
     */
    suspend fun updateCategoryServings(
        date: LocalDate,
        categoryId: CategoryId,
        servingCount: Int
    ): Result<Unit> {
        return try {
            database.transaction {
                val now = Clock.System.now().toEpochMilliseconds()

                // Get or create daily servings record
                var existing = queries.getDailyServingsRecordForDate(
                    date.toString()
                ).executeAsOneOrNull()

                val dailyServingsId: Long

                if (existing == null) {
                    // Create new daily record with current active suite
                    queries.insertDailyServings(
                        date = date.toString(),
                        suite_id = _activeSuite.value.id.value,
                        created_at = now,
                        updated_at = now
                    )
                    dailyServingsId = queries.lastInsertRowId().executeAsOne()
                } else {
                    dailyServingsId = existing.id
                    // Update timestamp
                    queries.updateDailyServings(
                        suite_id = existing.suite_id,
                        updated_at = now,
                        id = dailyServingsId
                    )
                }

                // Upsert category servings
                if (servingCount > 0) {
                    queries.upsertCategoryServings(
                        daily_servings_id = dailyServingsId,
                        category_id = categoryId.value,
                        serving_count = servingCount.toLong()
                    )
                } else {
                    queries.deleteCategoryServings(
                        daily_servings_id = dailyServingsId,
                        category_id = categoryId.value
                    )
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete servings for a specific date.
     */
    suspend fun deleteServingsForDate(date: LocalDate): Result<Unit> {
        return try {
            queries.deleteDailyServingsByDate(date.toString())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Calculate score for a specific date.
     * Returns the score and the suite used (for historical data).
     */
    suspend fun calculateScoreForDate(date: LocalDate): Result<DailyScoreResult?> {
        return getServingsForDate(date).map { dailyServings ->
            if (dailyServings == null) return@map null

            val suite = SuiteDefinitions.getSuiteById(dailyServings.suiteId)
            if (suite == null) {
                // Historical data with unknown suite
                return@map null
            }

            val score = dailyServings.calculateScore(suite) ?: 0
            DailyScoreResult(
                date = date,
                score = score,
                suiteId = suite.id,
                suiteName = suite.name,
                maxPossibleScore = suite.maxPossibleDailyScore
            )
        }
    }

    /**
     * Get count of days tracked.
     */
    suspend fun getDaysTrackedCount(): Result<Long> {
        return try {
            val count = queries.countDaysTracked().executeAsOne()
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get all dates that have records.
     */
    suspend fun getAllDatesWithRecords(): Result<List<LocalDate>> {
        return try {
            val dates = queries.getAllDatesWithRecords().executeAsList()
                .map { LocalDate.parse(it) }
            Result.success(dates)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
