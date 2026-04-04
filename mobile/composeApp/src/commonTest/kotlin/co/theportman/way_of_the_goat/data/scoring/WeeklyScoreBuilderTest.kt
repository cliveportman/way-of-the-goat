package co.theportman.way_of_the_goat.data.scoring

import co.theportman.way_of_the_goat.data.scoring.model.CategoryId
import co.theportman.way_of_the_goat.data.scoring.model.DailyServings
import co.theportman.way_of_the_goat.data.scoring.model.SuiteId
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WeeklyScoreBuilderTest {

    private val fixedTimeZone = TimeZone.UTC
    // 2026-02-18 is a Wednesday
    private val fixedClock = object : Clock {
        override fun now(): Instant = Instant.parse("2026-02-18T12:00:00Z")
    }
    private val builder = WeeklyScoreBuilder(clock = fixedClock, timeZone = fixedTimeZone)

    private fun servingsForDate(
        date: LocalDate,
        servings: Map<String, Int> = mapOf("fruits" to 3)
    ): DailyServings {
        return DailyServings(
            date = date,
            suiteId = SuiteDefinitions.BALANCED_ID,
            servings = servings.mapKeys { CategoryId(it.key) }
        )
    }

    @Test
    fun `getMonday returns Monday for any day of the week`() {
        // Monday 2026-02-16
        assertEquals(LocalDate(2026, 2, 16), WeeklyScoreBuilder.getMonday(LocalDate(2026, 2, 16)))
        // Tuesday
        assertEquals(LocalDate(2026, 2, 16), WeeklyScoreBuilder.getMonday(LocalDate(2026, 2, 17)))
        // Wednesday
        assertEquals(LocalDate(2026, 2, 16), WeeklyScoreBuilder.getMonday(LocalDate(2026, 2, 18)))
        // Thursday
        assertEquals(LocalDate(2026, 2, 16), WeeklyScoreBuilder.getMonday(LocalDate(2026, 2, 19)))
        // Friday
        assertEquals(LocalDate(2026, 2, 16), WeeklyScoreBuilder.getMonday(LocalDate(2026, 2, 20)))
        // Saturday
        assertEquals(LocalDate(2026, 2, 16), WeeklyScoreBuilder.getMonday(LocalDate(2026, 2, 21)))
        // Sunday
        assertEquals(LocalDate(2026, 2, 16), WeeklyScoreBuilder.getMonday(LocalDate(2026, 2, 22)))
    }

    @Test
    fun `formatDateRange same month`() {
        val label = WeeklyScoreBuilder.formatDateRange(LocalDate(2026, 2, 16))
        assertEquals("Feb 16-22", label)
    }

    @Test
    fun `formatDateRange cross month`() {
        val label = WeeklyScoreBuilder.formatDateRange(LocalDate(2026, 1, 26))
        assertEquals("Jan 26-Feb 1", label)
    }

    @Test
    fun `buildWeeks returns null for empty map`() {
        assertNull(builder.buildWeeks(emptyMap()))
    }

    @Test
    fun `buildWeeks groups data into correct weeks`() {
        val servingsMap = mapOf(
            LocalDate(2026, 2, 16) to servingsForDate(LocalDate(2026, 2, 16)),
            LocalDate(2026, 2, 10) to servingsForDate(LocalDate(2026, 2, 10))
        )

        val weeks = builder.buildWeeks(servingsMap)
        assertNotNull(weeks)
        // Current week (Feb 16-22) + previous week (Feb 9-15) + week of Feb 10 (same as prev)
        // fixedClock is Feb 18 (Wed), so current week Monday is Feb 16
        // Feb 10 is a Tuesday, its Monday is Feb 9
        // So we expect weeks from Feb 16 back to Feb 9 = 2 weeks
        assertEquals(2, weeks.size)
        assertEquals("Feb 16-22", weeks[0].dateRangeLabel)
        assertEquals("Feb 9-15", weeks[1].dateRangeLabel)
    }

    @Test
    fun `buildWeeks returns most recent week first`() {
        val servingsMap = mapOf(
            LocalDate(2026, 2, 2) to servingsForDate(LocalDate(2026, 2, 2)),
            LocalDate(2026, 2, 16) to servingsForDate(LocalDate(2026, 2, 16))
        )

        val weeks = builder.buildWeeks(servingsMap)
        assertNotNull(weeks)
        assertTrue(weeks.size >= 2)
        // First week should be the most recent (contains Feb 16)
        assertEquals("Feb 16-22", weeks.first().dateRangeLabel)
    }

    @Test
    fun `buildWeekData has 7 entries Mon through Sun`() {
        val monday = LocalDate(2026, 2, 16)
        val servingsMap = mapOf(
            monday to servingsForDate(monday)
        )

        val weekData = builder.buildWeekData(monday, servingsMap)
        assertEquals(7, weekData.dailyScores.size)
        // Monday has a score, rest are null
        assertNotNull(weekData.dailyScores[0])
        assertEquals("Monday", weekData.dailyScores[0]!!.dayName)
        for (i in 1..6) {
            assertNull(weekData.dailyScores[i])
        }
    }

    @Test
    fun `buildWeekData calculates weekly total from scored days only`() {
        val monday = LocalDate(2026, 2, 16)
        val tuesday = LocalDate(2026, 2, 17)
        val servingsMap = mapOf(
            monday to servingsForDate(monday),
            tuesday to servingsForDate(tuesday)
        )

        val weekData = builder.buildWeekData(monday, servingsMap)
        // Total should be sum of the two scored days
        val expectedTotal = weekData.dailyScores.filterNotNull().sumOf { it.score }
        assertEquals(expectedTotal, weekData.weeklyTotal)
    }

    @Test
    fun `dayNameForDayOfWeek returns correct names`() {
        assertEquals("Monday", WeeklyScoreBuilder.dayNameForDayOfWeek(kotlinx.datetime.DayOfWeek.MONDAY))
        assertEquals("Sunday", WeeklyScoreBuilder.dayNameForDayOfWeek(kotlinx.datetime.DayOfWeek.SUNDAY))
    }
}
